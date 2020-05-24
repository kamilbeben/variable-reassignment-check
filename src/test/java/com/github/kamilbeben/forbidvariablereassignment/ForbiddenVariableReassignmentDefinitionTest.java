package com.github.kamilbeben.forbidvariablereassignment;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;

import static com.github.kamilbeben.forbidvariablereassignment.ForbiddenVariableReassignmentDefinition.REPOSITORY_KEY;

public class ForbiddenVariableReassignmentDefinitionTest {

  @Test
  public void test() {
    final RulesDefinition.Context context = new RulesDefinition.Context();
    final ForbiddenVariableReassignmentDefinition definition = new ForbiddenVariableReassignmentDefinition();
    definition.define(context);

    final RulesDefinition.Repository repository = context.repository(REPOSITORY_KEY);
    Assert.assertEquals(1l, repository.rules().size());

    final RulesDefinition.Rule rule = repository.rules().get(0);
    Assert.assertNotNull(rule);
    Assert.assertFalse(rule.params().isEmpty());
    Assert.assertTrue(rule.params().stream().map(RulesDefinition.Param::description).allMatch(StringUtils::isNotBlank));
  }

}