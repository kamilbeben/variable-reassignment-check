package com.github.kamilbeben.forbidvariablereassignment.check;

import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class CheckTest {

  @Test
  public void basicTest() {
    Check check = constructCheck();
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

  public Check constructCheck() {
    Check check = new Check();

    check.mutableAnnotationName = Utils.DEFAULT_MUTABLE_ANNOTATION_NAME;
    check.variableReassignedMessageTemplate = Utils.DEFAULT_VARIABLE_REASSIGNED_MESSAGE_TEMPLATE;
    check.variableReassignedInsideLoopMessageTemplate = Utils.DEFAULT_VARIABLE_REASSIGNED_INSIDE_LOOP_MESSAGE_TEMPLATE;
    check.forbidVariableReassignmentInsideLoop = Boolean.valueOf(Utils.DEFAULT_FORBID_VARIABLE_REASSIGNMENT_INSIDE_LOOP);
    check.forbidVariableReassignment = Boolean.valueOf(Utils.DEFAULT_FORBID_VARIABLE_REASSIGNMENT);

    return check;
  }

}
