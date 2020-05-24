package com.github.kamilbeben.variablereassignmentcheck.pojo;

import com.github.kamilbeben.variablereassignmentcheck.Utils;
import org.sonar.plugins.java.api.tree.Tree;

public class AssignationExpression extends BlockChild {

  private final Variable variable;
  private final boolean isInsideLoop;
  private final boolean isInsideLoopParenthesis;

  AssignationExpression(Block parent, Variable variable, Tree tree) {
    super(parent, tree.firstToken(), tree.lastToken());
    this.variable = variable;
    this.isInsideLoop = Utils.isInsideLoop(tree);
    this.isInsideLoopParenthesis = Utils.isInsideLoopParenthesis(tree);
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
