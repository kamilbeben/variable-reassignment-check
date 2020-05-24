package com.github.kamilbeben.forbidvariablereassignment.check.internal;

import com.github.kamilbeben.forbidvariablereassignment.check.ForbiddenVariableReassignmentUtils;
import org.sonar.plugins.java.api.tree.Tree;

public class ValueAssignationExpression extends BlockChild {

  private final Variable variable;
  private final boolean isInsideLoop;
  private final boolean isInsideLoopParenthesis;

  ValueAssignationExpression(Block parent, Variable variable, Tree tree) {
    super(parent, tree.firstToken(), tree.lastToken());
    this.variable = variable;
    this.isInsideLoop = ForbiddenVariableReassignmentUtils.isInsideLoop(tree);
    this.isInsideLoopParenthesis = ForbiddenVariableReassignmentUtils.isInsideLoopParenthesis(tree);
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

}
