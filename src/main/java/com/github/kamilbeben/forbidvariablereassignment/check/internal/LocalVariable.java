package com.github.kamilbeben.forbidvariablereassignment.check.internal;

import com.google.common.collect.ImmutableList;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.VariableTree;

import java.util.ArrayList;
import java.util.List;

public class LocalVariable extends BlockChild {

  private final String name;
  private final boolean isMutable;
  private final boolean hasInitialValue;
  private final List<ValueAssignationExpression> assignationExpressions = new ArrayList<>();

  protected LocalVariable(Block parent, VariableTree tree, boolean isMutable, boolean hasInitialValue) {
    super(parent, tree.firstToken(), tree.lastToken());
    this.name = tree.simpleName().name();
    this.isMutable = isMutable;
    this.hasInitialValue = hasInitialValue;
    parent.addChild(this);
  }

  public static LocalVariable create (Block parent, VariableTree tree, boolean mutable, boolean hasInitialValue) {
    return new LocalVariable(parent, tree, mutable, hasInitialValue);
  }

  public ValueAssignationExpression assignValue(Block parent, AssignmentExpressionTree assignmentExpressionTree) {
    final ValueAssignationExpression expression = new ValueAssignationExpression(parent, this, assignmentExpressionTree);
    assignationExpressions.add(expression);
    return expression;
  }

  public String name() {
    return name;
  }

  public boolean isMutable() {
    return isMutable;
  }

  public boolean isImmutable() {
    return !isMutable;
  }

  public boolean hasInitialValue() {
    return hasInitialValue;
  }

  public List<ValueAssignationExpression> assignationExpressions() {
    return ImmutableList.copyOf(assignationExpressions);
  }

}
