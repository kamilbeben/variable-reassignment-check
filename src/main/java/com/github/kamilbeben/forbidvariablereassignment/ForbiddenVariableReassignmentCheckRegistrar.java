package com.github.kamilbeben.forbidvariablereassignment;

import com.github.kamilbeben.forbidvariablereassignment.check.ForbiddenVariableReassignmentCheck;
import com.google.common.collect.ImmutableList;
import org.sonar.plugins.java.api.CheckRegistrar;
import org.sonarsource.api.sonarlint.SonarLintSide;

import java.util.Collections;

import static com.github.kamilbeben.forbidvariablereassignment.ForbiddenVariableReassignmentDefinition.REPOSITORY_KEY;

@SonarLintSide
public class ForbiddenVariableReassignmentCheckRegistrar implements CheckRegistrar {

  @Override
  public void register(RegistrarContext registrarContext) {
    registrarContext.registerClassesForRepository(
      REPOSITORY_KEY,
      ImmutableList.of(ForbiddenVariableReassignmentCheck.class),
      Collections.emptyList()
    );
  }

}
