package com.github.kamilbeben.forbidvariablereassignment.check;

import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class ForbiddenVariableReassignmentCheckTest {

  @Test
  public void basicTest() {
    ForbiddenVariableReassignmentCheck check = constructCheck();
    check.mutableAnnotationName = "CustomMutable";

    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/forbidvariablereassignment/BasicTestCase.java")
      .withCheck(check)
      .verifyIssues();
  }

  @Test
  public void ifConditionTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/forbidvariablereassignment/IfTestCase.java")
      .withCheck(constructCheck())
      .verifyIssues();
  }

  @Test
  public void switchTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/forbidvariablereassignment/SwitchTestCase.java")
      .withCheck(constructCheck())
      .verifyIssues();
  }

  @Test
  public void loopTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/forbidvariablereassignment/LoopTestCase.java")
      .withCheck(constructCheck())
      .verifyIssues();
  }

  @Test
  public void orderStatusTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/forbidvariablereassignment/OrderStatus.java")
      .withCheck(constructCheck())
      .verifyIssues();
  }

  public ForbiddenVariableReassignmentCheck constructCheck() {
    ForbiddenVariableReassignmentCheck check = new ForbiddenVariableReassignmentCheck();

    check.mutableAnnotationName = ForbiddenVariableReassignmentUtils.DEFAULT_MUTABLE_ANNOTATION_NAME;
    check.variableReassignedMessageTemplate = ForbiddenVariableReassignmentUtils.DEFAULT_VARIABLE_REASSIGNED_MESSAGE_TEMPLATE;
    check.methodParameterReassignedMessageTemplate = ForbiddenVariableReassignmentUtils.DEFAULT_METHOD_PARAMETER_REASSIGNED_MESSAGE_TEMPLATE;
    check.variableReassignedInsideLoopMessageTemplate = ForbiddenVariableReassignmentUtils.DEFAULT_VARIABLE_REASSIGNED_INSIDE_LOOP_MESSAGE_TEMPLATE;
    check.forbidReassignmentInsideLoop = Boolean.valueOf(ForbiddenVariableReassignmentUtils.DEFAULT_FORBID_REASSIGNMENT_INSIDE_LOOP);
    check.forbidLocalVariableReassignment = Boolean.valueOf(ForbiddenVariableReassignmentUtils.DEFAULT_FORBID_LOCAL_VARIABLE_REASSIGNMENT);
    check.forbidMethodParameterReassignment = Boolean.valueOf(ForbiddenVariableReassignmentUtils.DEFAULT_FORBID_METHOD_PARAMETER_REASSIGNMENT);

    return check;
  }

}
