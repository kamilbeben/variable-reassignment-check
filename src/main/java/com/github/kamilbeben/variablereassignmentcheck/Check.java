package com.github.kamilbeben.variablereassignmentcheck;

import com.github.kamilbeben.variablereassignmentcheck.pojo.AssignationExpression;
import com.github.kamilbeben.variablereassignmentcheck.pojo.Block;
import com.github.kamilbeben.variablereassignmentcheck.pojo.Variable;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;

import java.util.*;

import static com.github.kamilbeben.variablereassignmentcheck.Constant.*;
import static org.sonar.check.Priority.MINOR;
import static org.sonar.plugins.java.api.tree.Tree.Kind.ANNOTATION;

@Rule(
  key = Constant.Check.KEY,
  name = Constant.Check.NAME,
  description = Constant.Check.DESCRIPTION,
  priority = MINOR,
  tags = { "brain-overload", "confusing", "bad-practice" }
)
public class Check extends BaseTreeVisitor implements JavaFileScanner {

  @RuleProperty(
    defaultValue = Parameter.ReportLocalVariableReassignment.DEFAULT,
    description = Parameter.ReportLocalVariableReassignment.DESCRIPTION
  )
  boolean reportLocalVariableReassignment;

  @RuleProperty(
    defaultValue = Parameter.ReportMethodParameterReassignment.DEFAULT,
    description = Parameter.ReportMethodParameterReassignment.DESCRIPTION
  )
  boolean reportMethodParameterReassignment;

  @RuleProperty(
    defaultValue = Parameter.ReportReassignmentInsideLoop.DEFAULT,
    description = Parameter.ReportReassignmentInsideLoop.DESCRIPTION
  )
  boolean reportReassignmentInsideLoop;

  @RuleProperty(
    defaultValue = Parameter.MessageTemplate.DEFAULT,
    description = Parameter.MessageTemplate.DESCRIPTION
  )
  String messageTemplate;

  @RuleProperty(
    defaultValue = Parameter.MutableAnnotationName.DEFAULT,
    description = Parameter.MutableAnnotationName.DESCRIPTION
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

          Variable.createMethodParameter(parent, parameter, isMutable);
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
        .filter(Variable.class::isInstance)
        .map(Variable.class::cast)
        .map(Variable::name)
        .anyMatch(tree.simpleName().name()::equals);

      if (isAlreadyDefined) return; // possibly it is a method parameter which was defined in `visitMethod`

      final boolean hasInitialValue = tree.initializer() != null;
      final boolean isMutable = isAnnotatedByConfiguredAnnotation(tree);

      Variable.createLocalVariable(parent, tree, isMutable, hasInitialValue);

    } finally {
      super.visitVariable(tree);
    }
  }

  @Override
  public void visitUnaryExpression(UnaryExpressionTree tree) {
    try {
      if (!tree.is(HANDLED_UNARY_OPERATOR)) return;

      final String variableName = getVariableName(tree.expression());
      final Variable variable = rootBlocks.peek().findVariable(variableName, tree);

      if (variable == null) return;

      reportErrorIfAssignmentWasIllegal(variable.assignValue(rootBlocks.peek().nearestBlock(tree), tree));

    } finally {
      super.visitUnaryExpression(tree);
    }
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    try {
      final String variableName = getVariableName(tree.variable());
      final Variable variable = rootBlocks.peek().findVariable(variableName, tree);

      if (variable == null) return; // it means it's not local variable nor method parameter

      reportErrorIfAssignmentWasIllegal(variable.assignValue(rootBlocks.peek().nearestBlock(tree), tree));

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

        final boolean hasBreakOrReturnStatement = caseGroupTree.body().stream()
          .anyMatch(it -> it.is(BREAK_OUT_OF_SWITCH_EXPRESSION));

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
        return null; // member fields are not handled by this check
      case IDENTIFIER:
        return ((IdentifierTree) tree).name();
      default:
        System.err.println("Couldn't get variable name out of tree of kind " + tree.kind() + " at line " + tree.firstToken().line());
        return null;
    }
  }

  private void reportErrorIfAssignmentWasIllegal(AssignationExpression expression) {
    final Variable variable = expression.variable();

    final boolean handles =
      (expression.isInsideLoop() && reportReassignmentInsideLoop) ||
      (variable.isLocal() && reportLocalVariableReassignment) ||
      (variable.isMethodParameter() && reportMethodParameterReassignment);

    if (variable.isMutable() || !handles) return;

    if (expression.isInsideLoop()) {
      if (expression.isInsideLoopParenthesis() && variable.isInsideLoopParenthesis()) return;

      if (!variable.isInsideLoop()) {
        reportIssue(expression);
        return;
      }
    }

    final List<AssignationExpression> expressions = variable.assignationExpressions();

    if (variable.hasInitialValue() || variable.isMethodParameter()) {
      reportIssue(expression);
      return;
    }

    for (AssignationExpression previousAssignationExpression : expressions) {
      if (previousAssignationExpression == expression ||
          areExpressionsMutuallyExclusive(previousAssignationExpression, expression)) continue;

      reportIssue(expression);
      return;
    }
  }

  private boolean areExpressionsMutuallyExclusive(AssignationExpression a, AssignationExpression b) {
    final Block closestCommonAncestor = getClosestCommonAncestor(a, b);

    return
      closestCommonAncestor == null ||
      closestCommonAncestor.type() == Block.Type.MUTUALLY_EXCLUSIVE_STATEMENTS_WRAPPER;
  }

  private Block getClosestCommonAncestor(AssignationExpression a, AssignationExpression b) {
    final List<Block> ancestorsOfA = a.ancestorsClosestToFurthest();
    final List<Block> ancestorsOfB = b.ancestorsClosestToFurthest();

    return ancestorsOfA.stream()
      .filter(ancestorsOfB::contains)
      .findFirst()
      .orElse(null);
  }

  private void reportIssue(AssignationExpression expression) {
    int line = expression.firstToken().line();

    final String message = Optional
      .ofNullable(messageTemplate)
      .orElse(Parameter.MessageTemplate.DEFAULT)
        .replace(Parameter.MessageTemplate.PARAM_VARIABLE_NAME, expression.variable().name());

    fileScannerContext.addIssue(line, this, message);
  }
}
