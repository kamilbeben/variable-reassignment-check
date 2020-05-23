package com.github.kamilbeben.forbidvariablereassignment;

import com.github.kamilbeben.forbidvariablereassignment.check.Check;
import com.google.common.collect.ImmutableList;
import org.sonarsource.api.sonarlint.SonarLintSide;

import java.util.Collections;

import static com.github.kamilbeben.forbidvariablereassignment.RulesDefinition.REPOSITORY_KEY;

@SonarLintSide
public class CheckRegistrar implements org.sonar.plugins.java.api.CheckRegistrar {

  @Override
  public void register(RegistrarContext registrarContext) {
    registrarContext.registerClassesForRepository(
      REPOSITORY_KEY,
      ImmutableList.of(Check.class),
      Collections.emptyList()
    );
  }

}
