package com.github.kamilbeben.forbidvariablereassignment;

import org.sonar.api.server.rule.RulesDefinition;

public class ForbiddenVariableReassignmentDefinition implements RulesDefinition {

  static final String REPOSITORY_KEY = "com.github.kamilbeben";
  private static final String REPOSITORY_NAME = "kamilbeben";
  private static final String LANGUAGE = "java";

  @Override
  public void define(Context context) {
    final NewRepository repository = context
      .createRepository(REPOSITORY_KEY, LANGUAGE)
      .setName(REPOSITORY_NAME);

    repository.done();
  }
}
