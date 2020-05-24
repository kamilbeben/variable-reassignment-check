package com.github.kamilbeben.variablereassignmentcheck;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;

import static com.github.kamilbeben.variablereassignmentcheck.Definition.REPOSITORY_KEY;

public class DefinitionTest {

  @Test
  public void test() {
    final RulesDefinition.Context context = new RulesDefinition.Context();
    final Definition definition = new Definition();
    definition.define(context);

    final RulesDefinition.Repository repository = context.repository(REPOSITORY_KEY);
    Assert.assertEquals(1l, repository.rules().size());

    final RulesDefinition.Rule rule = repository.rules().get(0);
    Assert.assertNotNull(rule);
    Assert.assertFalse(rule.params().isEmpty());
    Assert.assertTrue(rule.params().stream().map(RulesDefinition.Param::description).allMatch(StringUtils::isNotBlank));
  }

}