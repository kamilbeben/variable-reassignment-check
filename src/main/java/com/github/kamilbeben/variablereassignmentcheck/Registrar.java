package com.github.kamilbeben.variablereassignmentcheck;

import com.google.common.collect.ImmutableList;
import org.sonar.plugins.java.api.CheckRegistrar;
import org.sonarsource.api.sonarlint.SonarLintSide;

import java.util.Collections;

import static com.github.kamilbeben.variablereassignmentcheck.Definition.REPOSITORY_KEY;

@SonarLintSide
public class Registrar implements CheckRegistrar {

  @Override
  public void register(RegistrarContext registrarContext) {
    registrarContext.registerClassesForRepository(
      REPOSITORY_KEY,
      ImmutableList.of(Check.class),
      Collections.emptyList()
    );
  }

}
