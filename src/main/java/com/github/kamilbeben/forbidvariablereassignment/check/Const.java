package com.github.kamilbeben.forbidvariablereassignment.check;

import com.google.common.collect.ImmutableSet;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.Set;

import static org.sonar.plugins.java.api.tree.Tree.Kind.*;

public class Const {

  static final String PARAM_VARIABLE_NAME = "{variableName}";
  static final String PARAM_LINE_NUMBER = "{lineNumber}";
  static final String PARAM_COLUMN_NUMBER = "{columnNumber}";
  static final String TRUE = "true";

  static final String DEFAULT_VARIABLE_REASSIGNED_INSIDE_LOOP_MESSAGE_TEMPLATE = PARAM_VARIABLE_NAME + " was reassigned inside loop at line " + PARAM_LINE_NUMBER + ".";
  static final String DEFAULT_VARIABLE_REASSIGNED_MESSAGE_TEMPLATE = PARAM_VARIABLE_NAME + " was reassigned at line " + PARAM_LINE_NUMBER + ".";
  static final String DEFAULT_FORBID_VARIABLE_REASSIGNMENT_INSIDE_LOOP = TRUE;
  static final String DEFAULT_FORBID_VARIABLE_REASSIGNMENT = TRUE;
  static final String DEFAULT_MUTABLE_ANNOTATION_NAME = "Mutable";

  static Set<Tree.Kind> LOOP_TREE_KINDS = ImmutableSet.of(WHILE_STATEMENT, DO_STATEMENT, FOR_STATEMENT, FOR_EACH_STATEMENT);
  static Set<Tree.Kind> BREAK_OUT_OF_SWITCH_EXPRESSION_KINDS = ImmutableSet.of(BREAK_STATEMENT, RETURN_STATEMENT);
}
