package com.github.kamilbeben.forbidvariablereassignment.check;

import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class CheckTest {

  @Test
  public void basicTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/forbidvariablereassignment/BasicTestCase.java")
      .withCheck(new Check())
      .verifyIssues();
  }

  @Test
  public void ifConditionTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/forbidvariablereassignment/IfTestCase.java")
      .withCheck(new Check())
      .verifyIssues();
  }

  @Test
  public void switchTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/forbidvariablereassignment/SwitchTestCase.java")
      .withCheck(new Check())
      .verifyIssues();
  }

  @Test
  public void loopTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/forbidvariablereassignment/LoopTestCase.java")
      .withCheck(new Check())
      .verifyIssues();
  }

  @Test
  public void orderStatusTest() {
    JavaCheckVerifier.newVerifier()
      .onFile("src/test/files/forbidvariablereassignment/OrderStatus.java")
      .withCheck(new Check())
      .verifyIssues();
  }

}
