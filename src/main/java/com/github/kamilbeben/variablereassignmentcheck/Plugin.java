package com.github.kamilbeben.variablereassignmentcheck;

public class Plugin implements org.sonar.api.Plugin {

  @Override
  public void define(Context context) {
    context.addExtension(Definition.class);
    context.addExtension(Registrar.class);
  }

}
