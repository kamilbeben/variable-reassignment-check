package com.github.kamilbeben.forbidvariablereassignment;

import org.sonar.api.Plugin;

public class ForbiddenVariableReassignmentPlugin implements Plugin {

  @Override
  public void define(Context context) {
    context.addExtension(ForbiddenVariableReassignmentDefinition.class);
    context.addExtension(ForbiddenVariableReassignmentCheckRegistrar.class);
  }

}
