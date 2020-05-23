package com.github.kamilbeben.forbidvariablereassignment.check;

import com.github.kamilbeben.forbidvariablereassignment.check.internal.Block;
import com.github.kamilbeben.forbidvariablereassignment.check.internal.LocalVariable;
import com.github.kamilbeben.forbidvariablereassignment.check.internal.ValueAssignationExpression;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;

import java.util.*;

import static com.github.kamilbeben.forbidvariablereassignment.check.ForbiddenVariableReassignmentUtils.*;
import static org.sonar.check.Priority.MINOR;
import static org.sonar.plugins.java.api.tree.Tree.Kind.ANNOTATION;

@Rule(
  key = CHECK_NAME,
  description = CHECK_DESCRIPTION,
  priority = MINOR
)
public class ForbiddenVariableReassignmentCheck extends BaseTreeVisitor implements JavaFileScanner {

  // TODO registrar
  // TODO html example

  @RuleProperty(
    defaultValue = DEFAULT_VARIABLE_REASSIGNED_MESSAGE_TEMPLATE,
    description = VARIABLE_REASSIGNED_MESSAGE_TEMPLATE_DESCRIPTION
  )
  String variableReassignedMessageTemplate;

  @RuleProperty(
    defaultValue = DEFAULT_VARIABLE_REASSIGNED_INSIDE_LOOP_MESSAGE_TEMPLATE,
    description = VARIABLE_REASSIGNED_INSIDE_LOOP_MESSAGE_TEMPLATE_DESCRIPTION
  )
  String variableReassignedInsideLoopMessageTemplate;

  @RuleProperty(
    defaultValue = DEFAULT_FORBID_VARIABLE_REASSIGNMENT,
    description = FORBID_VARIABLE_REASSIGNMENT_DESCRIPTION
  )
  boolean forbidVariableReassignment;

  @RuleProperty(
    defaultValue = DEFAULT_FORBID_VARIABLE_REASSIGNMENT_INSIDE_LOOP,
    description = FORBID_VARIABLE_REASSIGNMENT_INSIDE_LOOP_DESCRIPTION
  )
  boolean forbidVariableReassignmentInsideLoop;

  @RuleProperty(
    defaultValue = DEFAULT_MUTABLE_ANNOTATION_NAME,
    description = MUTABLE_ANNOTATION_DESCRIPTION
  )
  String mutableAnnotationName;

  // blocks (static blocks, methods) which are direct children of the class
  private final Deque<Block> rootBlocks = new ArrayDeque<>();

  private JavaFileScannerContext fileScannerContext;

  @Override
  public void scanFile(JavaFileScannerContext context) {
    fileScannerContext = context;
    scan(context.getTree());
  }

  @Override
  public void visitMethod(MethodTree tree) {
    try {
      rootBlocks.push(Block.create(null, tree, Block.Type.INEVITABLE));

      tree.parameters()
        .forEach(parameter -> {

          final Block parent = rootBlocks.peek().nearestBlock(parameter);
          final boolean isMutable = isAnnotatedByConfiguredAnnotation(parameter);

          LocalVariable.create(parent, parameter, isMutable, true);
        });

    } finally {
      super.visitMethod(tree);
      rootBlocks.pop();
    }
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

      final boolean isAlreadyDefined = parent.children().stream()
        .filter(LocalVariable.class::isInstance)
        .map(LocalVariable.class::cast)
        .map(LocalVariable::name)
        .anyMatch(tree.simpleName().name()::equals);

      if (isAlreadyDefined) return; // possibly it is a method parameter and was defined in `visitMethod`

      final boolean hasInitialValue = tree.initializer() != null;
      final boolean isMutable = isAnnotatedByConfiguredAnnotation(tree);

      LocalVariable.create(parent, tree, isMutable, hasInitialValue);
    } finally {
      super.visitVariable(tree);
    }
  }

  @Override
  public void visitUnaryExpression(UnaryExpressionTree tree) {
    try {
      if (!tree.is(HANDLED_UNARY_OPERATOR)) return;

      final String variableName = getVariableName(tree.expression());
      final LocalVariable variable = rootBlocks.peek().findVariable(variableName, tree);

      if (variable == null) return;

      reportErrorIfAssignmentWasIllegal(
        variable,
        variable.assignValue(rootBlocks.peek().nearestBlock(tree), tree),
        tree
      );

    } finally {
      super.visitUnaryExpression(tree);
    }
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    try {

      if (rootBlocks.isEmpty()) return;  // we're probably in a class, enum or something like that
      if (KEYWORD_THIS.equals(tree.firstToken().text())) return; // we're dealing with a class member, not local variable

      final String variableName = getVariableName(tree.variable());
      final LocalVariable variable = rootBlocks.peek().findVariable(variableName, tree);

      // it means it's not defined at all or not local
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

        final boolean hasBreakOrReturnStatement = caseGroupTree.body().stream().anyMatch(it -> it.is(BREAK_OUT_OF_SWITCH_EXPRESSION));

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

  private boolean isAnnotatedByConfiguredAnnotation(VariableTree tree) {

    if (StringUtils.isBlank(mutableAnnotationName)) return false;

    return tree.modifiers().stream()
      .filter(modifier -> modifier.kind() == ANNOTATION)
      .map(AnnotationTree.class::cast)
      .map(annotationTree -> annotationTree.annotationType().symbolType().name())
      .anyMatch(it -> Objects.equals(it, mutableAnnotationName));
  }

  private String getVariableName(ExpressionTree tree) {

    switch (tree.kind()) {
      case MEMBER_SELECT:
        return ((MemberSelectExpressionTree) tree).identifier().name();
      case IDENTIFIER:
        return ((IdentifierTree) tree).name();
      default:
        System.err.println("Couldn't get variable name out of tree of kind " + tree.kind() + " at line " + tree.firstToken().line());
        return null;
    }
  }

  private boolean isInsideLoop(Tree cursor) {
    return recursivelyGetParentLoopStatementTree(cursor) != null;
  }

  private boolean isInsideLoopParenthesis(Tree cursor) {
    final Tree loopStatementTree = recursivelyGetParentLoopStatementTree(cursor);
    if (loopStatementTree == null) return false;

    final SyntaxToken openToken;
    final SyntaxToken closeToken;

    switch (loopStatementTree.kind()) {
      case WHILE_STATEMENT:
        openToken = ((WhileStatementTree) loopStatementTree).openParenToken();
        closeToken = ((WhileStatementTree) loopStatementTree).closeParenToken();
        break;
      case DO_STATEMENT:
        openToken = ((DoWhileStatementTree) loopStatementTree).openParenToken();
        closeToken = ((DoWhileStatementTree) loopStatementTree).closeParenToken();
        break;
      case FOR_STATEMENT:
        openToken = ((ForStatementTree) loopStatementTree).openParenToken();
        closeToken = ((ForStatementTree) loopStatementTree).closeParenToken();
        break;
      case FOR_EACH_STATEMENT:
        openToken = ((ForEachStatement) loopStatementTree).openParenToken();
        closeToken = ((ForEachStatement) loopStatementTree).closeParenToken();
        break;
      default:
        return false;
    }

    return isWithin(openToken, closeToken, cursor);
  }

  private Tree recursivelyGetParentLoopStatementTree(Tree cursor) {
    for (
      Tree parent = cursor.parent();
      parent != null;
      parent = parent.parent()
    ) {
      if (parent.is(LOOP_TREE)) {
        return parent;
      }
    }
    return null;
  }

  private void reportErrorIfAssignmentWasIllegal(LocalVariable variable,
                                                 ValueAssignationExpression assignationExpression,
                                                 Tree tree) {

    if (forbidVariableReassignmentInsideLoop && variable.isImmutable() && isInsideLoop(tree)) {
      if (isInsideLoopParenthesis(tree)) {
        return;
      }
      reportIssue(assignationExpression, variableReassignedInsideLoopMessageTemplate);
      return;
    }

    if (!forbidVariableReassignment) return;

    final List<ValueAssignationExpression> expressions = variable.assignationExpressions();

    if (variable.isMutable()) return;

    if (variable.hasInitialValue()) {
      reportIssue(assignationExpression, variableReassignedMessageTemplate);
      return;
    }

    for (ValueAssignationExpression previousAssignationExpression : expressions) {
      if (previousAssignationExpression == assignationExpression ||
        areExpressionsMutuallyExclusive(previousAssignationExpression, assignationExpression)) continue;

      reportIssue(assignationExpression, variableReassignedMessageTemplate);
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

  private void reportIssue(ValueAssignationExpression expression, String messageTemplate) {

    int line = expression.firstToken().line();
    final String message = Optional.ofNullable(messageTemplate).orElse(DEFAULT_VARIABLE_REASSIGNED_MESSAGE_TEMPLATE)
      .replace(PARAM_VARIABLE_NAME, expression.localVariable().name())
      .replace(PARAM_LINE_NUMBER,   Integer.toString(line))
      .replace(PARAM_COLUMN_NUMBER, Integer.toString(expression.firstToken().column()));

    fileScannerContext.addIssue(line, this, message);
  }
}
