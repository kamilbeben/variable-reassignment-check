package com.github.kamilbeben.variablereassignmentcheck;

import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class CheckTest {

  @Test
  public void basicTest() {
    Check check = constructCheck();
    check.mutableAnnotationName = "CustomMutable";

    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/variablereassignmentcheck/BasicTestCase.java")
      .withCheck(check)
      .verifyIssues();
  }

  @Test
  public void ifConditionTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/variablereassignmentcheck/IfTestCase.java")
      .withCheck(constructCheck())
      .verifyIssues();
  }

  @Test
  public void switchTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/variablereassignmentcheck/SwitchTestCase.java")
      .withCheck(constructCheck())
      .verifyIssues();
  }

  @Test
  public void loopTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/variablereassignmentcheck/LoopTestCase.java")
      .withCheck(constructCheck())
      .verifyIssues();
  }

  @Test
  public void orderStatusTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/variablereassignmentcheck/OrderStatus.java")
      .withCheck(constructCheck())
      .verifyIssues();
  }

  public Check constructCheck() {
    Check check = new Check();

    check.mutableAnnotationName = Constant.Parameter.MutableAnnotationName.DEFAULT;
    check.messageTemplate = Constant.Parameter.MessageTemplate.DEFAULT;
    check.reportReassignmentInsideLoop = Boolean.valueOf(Constant.Parameter.TRUE);
    check.reportLocalVariableReassignment = Boolean.valueOf(Constant.Parameter.TRUE);
    check.reportMethodParameterReassignment = Boolean.valueOf(Constant.Parameter.TRUE);

    return check;
  }

}
