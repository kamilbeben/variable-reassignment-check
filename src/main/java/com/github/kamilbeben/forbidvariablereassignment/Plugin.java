package com.github.kamilbeben.forbidvariablereassignment;

/**
 * Entry point of your plugin containing your custom rules
 */
public class Plugin implements org.sonar.api.Plugin {

  @Override
  public void define(Context context) {
    context.addExtension(RulesDefinition.class);
    context.addExtension(CheckRegistrar.class);
  }

}
