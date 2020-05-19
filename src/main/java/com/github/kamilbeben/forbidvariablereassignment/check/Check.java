package com.github.kamilbeben.forbidvariablereassignment.check;

import com.github.kamilbeben.forbidvariablereassignment.check.internal.Block;
import com.github.kamilbeben.forbidvariablereassignment.check.internal.LocalVariable;
import com.github.kamilbeben.forbidvariablereassignment.check.internal.ValueAssignationExpression;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import java.util.*;

import static org.sonar.plugins.java.api.tree.Tree.Kind.*;

@Rule(
  key = "ForbidVariableReassignment"
)
public class Check extends BaseTreeVisitor implements JavaFileScanner {

  private static Set<Kind> LOOP_TREE_KINDS = ImmutableSet.of(WHILE_STATEMENT, DO_STATEMENT, FOR_STATEMENT, FOR_EACH_STATEMENT);
  private static Set<Kind> BREAK_OUT_OF_SWITCH_EXPRESSION_KINDS = ImmutableSet.of(BREAK_STATEMENT, RETURN_STATEMENT);

  // TODO configurable mutable annotation
  // TODO method parameters
  // TODO ++, -- lub opisz ze pomijasz i dlaczego pomijasz

  private final Deque<Block> rootBlocks = new ArrayDeque<>(); // static blocks, methods

  private JavaFileScannerContext fileScannerContext;

  @Override
  public void scanFile(JavaFileScannerContext context) {
    fileScannerContext = context;
    scan(context.getTree());
  }

  @Override
  public void visitBlock(BlockTree tree) {
    if (rootBlocks.isEmpty()) {
      visitRootBlock(tree);
    } else {
      visitRegularBlock(tree);
    }
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    try {
      final boolean isNotPresent = rootBlocks.peek().allDescendantBlocks().stream()
        .noneMatch(it -> it.startsAt(tree.firstToken()));

      if (isNotPresent) {
        createConditionalBlocks(tree);
      }
    } finally {
      super.visitIfStatement(tree);
    }
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    try {
      createConditionalBlocks(tree);
    } finally {
      super.visitSwitchStatement(tree);
    }
  }

  @Override
  public void visitVariable(VariableTree tree) {
    try {
      if (rootBlocks.isEmpty()) return; // we're probably in a class, enum or something like that

      final Block parent = rootBlocks.peek().nearestBlock(tree);
      final boolean hasInitialValue = tree.initializer() != null;
      final boolean isMutable = false;

      LocalVariable.create(parent, tree, isMutable, hasInitialValue);
    } finally {
      super.visitVariable(tree);
    }
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    try {

      if (rootBlocks.isEmpty()) return;  // we're probably in a class, enum or something like that

      final String variableName = getVariableName(tree);
      final LocalVariable variable = rootBlocks.peek().findVariable(variableName, tree);

      // it means it's either a method parameter, not defined att all or not local
      if (variable == null) return;

      reportErrorIfAssignmentWasIllegal(
        variable,
        variable.assignValue(rootBlocks.peek().nearestBlock(tree), tree),
        tree
      );

    } finally {
      super.visitAssignmentExpression(tree);
    }
  }

  private void visitRootBlock(BlockTree tree) {
    rootBlocks.push(Block.create(null, tree, Block.Type.INEVITABLE));
    super.visitBlock(tree);
    rootBlocks.pop();
  }

  private void visitRegularBlock(BlockTree tree) {
    try {
      final boolean isAlreadyDefined = rootBlocks.peek() // could be already defined by visitIfStatement / visitSwitchStatement
        .allDescendantBlocks().stream()
        .anyMatch(block ->
          block.startsAt(tree.firstToken()) &&
          block.endsAt(tree.lastToken())
        );

      if (isAlreadyDefined) return;

      final Block parent = rootBlocks.peek().nearestBlock(tree);

      Block.create(parent, tree, Block.Type.INEVITABLE);

    } finally {
      super.visitBlock(tree);
    }
  }

  private void createConditionalBlocks(IfStatementTree tree) {

    final Block parent = rootBlocks.peek().nearestBlock(tree);
    final Block wrapper = Block.create(parent, tree, Block.Type.MUTUALLY_EXCLUSIVE_STATEMENTS_WRAPPER);

    StatementTree elseStatement = tree;

    while (elseStatement instanceof IfStatementTree) {
      final IfStatementTree elseIfStatementTree = (IfStatementTree) elseStatement;
      final SyntaxToken firstToken = elseIfStatementTree.firstToken();
      final SyntaxToken lastToken = elseIfStatementTree.elseStatement() == null
        ? elseIfStatementTree.lastToken()
        : elseIfStatementTree.elseStatement().firstToken();

      Block.create(wrapper, firstToken, lastToken, Block.Type.CONDITIONAL);
      elseStatement = elseIfStatementTree.elseStatement();
    }

    if (elseStatement != null && !(elseStatement instanceof IfStatementTree)) {
      Block.create(wrapper, elseStatement, Block.Type.CONDITIONAL);
    }
  }

  private void createConditionalBlocks(SwitchStatementTree tree) {
    final List<List<CaseGroupTree>> mutuallyExclusiveCaseGroupTreeLists = extractMutuallyExclusiveCaseGroupTreeLists(tree);

    final Block wrapper = Block.create(rootBlocks.peek().nearestBlock(tree), tree, Block.Type.MUTUALLY_EXCLUSIVE_STATEMENTS_WRAPPER);

    mutuallyExclusiveCaseGroupTreeLists.stream()
      .forEach(list ->
        Block.create(
          wrapper,
          list.get(0).firstToken(),
          list.get(list.size() - 1).lastToken(),
          Block.Type.CONDITIONAL
        )
      );
  }

  private List<List<CaseGroupTree>> extractMutuallyExclusiveCaseGroupTreeLists(SwitchStatementTree tree) {

    final List<List<CaseGroupTree>> mutuallyExclusiveCaseGroupTreeLists = new ArrayList<>();
    final List<CaseGroupTree> mutuallyInclusiveCaseGroupTrees = new ArrayList<>();

    tree.cases()
      .forEach(caseGroupTree -> {
        mutuallyInclusiveCaseGroupTrees.add(caseGroupTree);

        final boolean hasBreakOrReturnStatement = caseGroupTree.body().stream().map(Tree::kind)
          .anyMatch(BREAK_OUT_OF_SWITCH_EXPRESSION_KINDS::contains);

        if (hasBreakOrReturnStatement) {
          mutuallyExclusiveCaseGroupTreeLists.add(ImmutableList.copyOf(mutuallyInclusiveCaseGroupTrees));
          mutuallyInclusiveCaseGroupTrees.clear();
        }
      });

    if (!mutuallyInclusiveCaseGroupTrees.isEmpty()) {
      mutuallyExclusiveCaseGroupTreeLists.add(ImmutableList.copyOf(mutuallyInclusiveCaseGroupTrees));
    }

    return mutuallyExclusiveCaseGroupTreeLists;
  }

  private String getVariableName(AssignmentExpressionTree tree) {
    final ExpressionTree variable = tree.variable();

    switch (variable.kind()) {
      case MEMBER_SELECT:
        return ((MemberSelectExpressionTree) variable).identifier().name();
      case IDENTIFIER:
        return ((IdentifierTree) variable).name();
      default:
        System.err.println("Couldn't get variable name out of tree of kind " + variable.kind() + " at line " + tree.firstToken().line());
        return null;
    }
  }

  private boolean isInsideLoop(Tree tree) {
    for (
      Tree parent = tree.parent();
      parent != null;
      parent = parent.parent()
    ) {
      if (LOOP_TREE_KINDS.contains(parent.kind())) {
        return true;
      }
    }
    return false;
  }

  private void reportErrorIfAssignmentWasIllegal(LocalVariable variable,
                                                 ValueAssignationExpression assignationExpression,
                                                 AssignmentExpressionTree tree) {

    if (isInsideLoop(tree)) {
      reportAssignInsideLoopStatement(assignationExpression);
      return;
    }

    final List<ValueAssignationExpression> expressions = variable.assignationExpressions();

    if (variable.isMutable()) return;

    if (variable.hasInitialValue()) {
      reportIllegalVariableReassign(assignationExpression);
      return;
    }

    for (ValueAssignationExpression previousAssignationExpression : expressions) {
      if (previousAssignationExpression == assignationExpression ||
        areExpressionsMutuallyExclusive(previousAssignationExpression, assignationExpression)) continue;

      reportIllegalVariableReassign(assignationExpression);
      return;
    }
  }

  private boolean areExpressionsMutuallyExclusive(ValueAssignationExpression a, ValueAssignationExpression b) {
    final Block closestCommonAncestor = getClosestCommonAncestor(a, b);
    return
      closestCommonAncestor == null ||
        closestCommonAncestor.type() == Block.Type.MUTUALLY_EXCLUSIVE_STATEMENTS_WRAPPER;
  }

  private Block getClosestCommonAncestor(ValueAssignationExpression a, ValueAssignationExpression b) {
    final List<Block> ancestorsOfA = a.ancestorsClosestToFurthest();
    final List<Block> ancestorsOfB = b.ancestorsClosestToFurthest();

    return ancestorsOfA.stream()
      .filter(ancestorsOfB::contains)
      .findFirst()
      .orElse(null);
  }

  private void reportAssignInsideLoopStatement(ValueAssignationExpression expression) {

    report(
      expression,
      String.format(
        "Variable %s was assigned inside loop statement at line %d.",
        expression.getLocalVariable().name(),
        expression.firstToken().line()
      )
    );
  }

  private void reportIllegalVariableReassign(ValueAssignationExpression expression) {

    report(
      expression,
      String.format(
        "Variable %s which is not marked as mutable was reassigned.",
        expression.getLocalVariable().name(),
        expression.firstToken().line()
      )
    );
  }

  private void report(ValueAssignationExpression expression, String message) {

    fileScannerContext.addIssue(expression.firstToken().line(), this, message);
  }
}
