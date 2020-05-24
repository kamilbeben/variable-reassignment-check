package com.github.kamilbeben.variablereassignmentcheck;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;

public class Definition implements RulesDefinition {

  static final String REPOSITORY_KEY = "com.github.kamilbeben";
  static final String REPOSITORY_NAME = "forbid-variable-reassignment-repository";
  static final String LANGUAGE = "java";

  private static final RulesDefinitionAnnotationLoader RULE_DEFINITION_LOADER = new RulesDefinitionAnnotationLoader();

  @Override
  public void define(Context context) {
    final NewRepository repository = context
      .createRepository(REPOSITORY_KEY, LANGUAGE)
      .setName(REPOSITORY_NAME);

    RULE_DEFINITION_LOADER
      .load(repository, Check.class);

    repository.done();
  }
}
