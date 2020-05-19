package com.github.kamilbeben.forbidvariablereassignment.check.internal;

import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;

public class ValueAssignationExpression extends BlockChild {

  private final LocalVariable localVariable;

  ValueAssignationExpression(Block parent, LocalVariable localVariable, AssignmentExpressionTree tree) {
    super(parent, tree.firstToken(), tree.lastToken());
    this.localVariable = localVariable;
    parent.addChild(this);
  }

  public LocalVariable getLocalVariable() {
    return localVariable;
  }
}
