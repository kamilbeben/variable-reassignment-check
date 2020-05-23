package com.github.kamilbeben.forbidvariablereassignment.check;

import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.Tree;

import static org.sonar.plugins.java.api.tree.Tree.Kind.*;

public class Utils {

  private Utils() {}

  public static final String CHECK_NAME = "ForbidVariableReassignment";
  public static final String CHECK_DESCRIPTION = ""; // TODO text

  static final String PARAM_VARIABLE_NAME = "{variableName}";
  static final String PARAM_LINE_NUMBER = "{lineNumber}";
  static final String PARAM_COLUMN_NUMBER = "{columnNumber}";
  static final String TRUE = "true";

  static final String DEFAULT_VARIABLE_REASSIGNED_INSIDE_LOOP_MESSAGE_TEMPLATE = PARAM_VARIABLE_NAME + " was reassigned inside loop at line " + PARAM_LINE_NUMBER + ".";
  static final String VARIABLE_REASSIGNED_INSIDE_LOOP_MESSAGE_TEMPLATE_DESCRIPTION = ""; // TODO text

  static final String DEFAULT_VARIABLE_REASSIGNED_MESSAGE_TEMPLATE = PARAM_VARIABLE_NAME + " was reassigned at line " + PARAM_LINE_NUMBER + ".";
  static final String VARIABLE_REASSIGNED_MESSAGE_TEMPLATE_DESCRIPTION = ""; // TODO text

  static final String DEFAULT_FORBID_VARIABLE_REASSIGNMENT_INSIDE_LOOP = TRUE;
  static final String FORBID_VARIABLE_REASSIGNMENT_INSIDE_LOOP_DESCRIPTION = ""; // TODO text

  static final String DEFAULT_FORBID_VARIABLE_REASSIGNMENT = TRUE;
  static final String FORBID_VARIABLE_REASSIGNMENT_DESCRIPTION = ""; // TODO text

  static final String DEFAULT_MUTABLE_ANNOTATION_NAME = "Mutable";
  static final String MUTABLE_ANNOTATION_DESCRIPTION = ""; // TODO text

  static Tree.Kind[] LOOP_TREE = { WHILE_STATEMENT, DO_STATEMENT, FOR_STATEMENT, FOR_EACH_STATEMENT };
  static Tree.Kind[] BREAK_OUT_OF_SWITCH_EXPRESSION = { BREAK_STATEMENT, RETURN_STATEMENT };
  static Tree.Kind[] HANDLED_UNARY_OPERATOR = { PREFIX_INCREMENT, PREFIX_DECREMENT, POSTFIX_DECREMENT, POSTFIX_INCREMENT };

  public static boolean isWithin(SyntaxToken boundaryStart, SyntaxToken boundaryEnd, Tree cursor) {
    final boolean startsBeforeCursor =
      boundaryStart.line() < cursor.firstToken().line() ||
        (
          boundaryStart.line()   == cursor.firstToken().line() &&
          boundaryStart.column() <= cursor.firstToken().column()
        );

    final boolean endsAfterCursor =
      boundaryEnd.line() > cursor.lastToken().line() ||
        (
          boundaryEnd.line()   == cursor.lastToken().line() &&
          boundaryEnd.column() >= cursor.lastToken().column()
        );

    return startsBeforeCursor && endsAfterCursor;
  }
}
