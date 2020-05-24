package com.github.kamilbeben.forbidvariablereassignment.check.internal;

import com.github.kamilbeben.forbidvariablereassignment.check.ForbiddenVariableReassignmentUtils;
import com.google.common.collect.ImmutableList;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

import java.util.ArrayList;
import java.util.List;

public class Variable extends BlockChild {

  enum Type {
    LOCAL_VARIABLE,
    METHOD_PARAMETER
  }

  private final String name;
  private final boolean isMutable;
  private final boolean hasInitialValue;
  private final boolean isInsideLoop;
  private final boolean isInsideLoopParenthesis;
  private final Type type;
  private final List<ValueAssignationExpression> assignationExpressions = new ArrayList<>();

  protected Variable(Block parent, VariableTree tree, boolean isMutable, boolean hasInitialValue, Type type) {
    super(parent, tree.firstToken(), tree.lastToken());
    this.name = tree.simpleName().name();
    this.isMutable = isMutable;
    this.hasInitialValue = hasInitialValue;
    this.type = type;
    this.isInsideLoop = ForbiddenVariableReassignmentUtils.isInsideLoop(tree);
    this.isInsideLoopParenthesis = ForbiddenVariableReassignmentUtils.isInsideLoopParenthesis(tree);
    parent.addChild(this);
  }

  public static Variable createLocalVariable(Block parent, VariableTree tree, boolean mutable, boolean hasInitialValue) {
    return new Variable(parent, tree, mutable, hasInitialValue, Type.LOCAL_VARIABLE);
  }

  public static Variable createMethodParameter(Block parent, VariableTree tree, boolean mutable) {
    return new Variable(parent, tree, mutable, false, Type.METHOD_PARAMETER);
  }

  public ValueAssignationExpression assignValue(Block parent, Tree tree) {
    final ValueAssignationExpression expression = new ValueAssignationExpression(parent, this, tree);
    assignationExpressions.add(expression);
    return expression;
  }

  public String name() {
    return name;
  }

  public boolean isMutable() {
    return isMutable;
  }

  public boolean hasInitialValue() {
    return hasInitialValue;
  }

  public boolean isLocal() {
    return type == Type.LOCAL_VARIABLE;
  }

  public boolean isMethodParameter() {
    return type == Type.METHOD_PARAMETER;
  }

  public boolean isInsideLoop() {
    return isInsideLoop;
  }

  public boolean isInsideLoopParenthesis() {
    return isInsideLoopParenthesis;
  }

  public List<ValueAssignationExpression> assignationExpressions() {
    return ImmutableList.copyOf(assignationExpressions);
  }

}
