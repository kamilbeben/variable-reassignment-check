package com.github.kamilbeben.forbidvariablereassignment.check.internal;

import org.sonar.plugins.java.api.tree.*;

import static com.github.kamilbeben.forbidvariablereassignment.check.ForbiddenVariableReassignmentUtils.LOOP_TREE;
import static com.github.kamilbeben.forbidvariablereassignment.check.ForbiddenVariableReassignmentUtils.isWithin;

public class ValueAssignationExpression extends BlockChild {

  private final Variable variable;
  private final boolean isInsideLoop;
  private final boolean isInsideLoopParenthesis;

  ValueAssignationExpression(Block parent, Variable variable, Tree tree) {
    super(parent, tree.firstToken(), tree.lastToken());
    this.variable = variable;
    this.isInsideLoop = isInsideLoop(tree);
    this.isInsideLoopParenthesis = isInsideLoopParenthesis(tree);
    parent.addChild(this);
  }

  public Variable variable() {
    return variable;
  }

  public boolean isInsideLoop() {
    return isInsideLoop;
  }

  public boolean isInsideLoopParenthesis() {
    return isInsideLoopParenthesis;
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
}
