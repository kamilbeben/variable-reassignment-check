package com.github.kamilbeben.forbidvariablereassignment.check;

import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.Tree;

import static org.sonar.plugins.java.api.tree.Tree.Kind.*;

public class ForbiddenVariableReassignmentUtils {

  private ForbiddenVariableReassignmentUtils() {}

  static final String CHECK_NAME = "forbid-variable-reassignment";
  static final String CHECK_DESCRIPTION = "Reassigning variables (oh, the irony) adds unnecessary layer of complexity to the code and, " +
    "in most cases it means that the code simply could be better (for example, some parts could be extracted to another method)";

  static final String PARAM_VARIABLE_NAME = "{variableName}";
  static final String PARAM_LINE_NUMBER = "{lineNumber}";
  static final String PARAM_COLUMN_NUMBER = "{columnNumber}";
  static final String TRUE = "true";

  static final String DEFAULT_VARIABLE_REASSIGNED_INSIDE_LOOP_MESSAGE_TEMPLATE = PARAM_VARIABLE_NAME + " was reassigned inside loop at line " + PARAM_LINE_NUMBER + ".";
  static final String VARIABLE_REASSIGNED_INSIDE_LOOP_MESSAGE_TEMPLATE_DESCRIPTION =
    "Message template that will be used to file an issue (reassignment inside loop). You can use following parameters " +
      PARAM_VARIABLE_NAME + ", " + PARAM_LINE_NUMBER + ", " + PARAM_COLUMN_NUMBER;

  static final String DEFAULT_VARIABLE_REASSIGNED_MESSAGE_TEMPLATE = PARAM_VARIABLE_NAME + " was reassigned at line " + PARAM_LINE_NUMBER + ".";
  static final String VARIABLE_REASSIGNED_MESSAGE_TEMPLATE_DESCRIPTION =
    "Message template that will be used to file an issue (variable reassignment). You can use following parameters " +
      PARAM_VARIABLE_NAME + ", " + PARAM_LINE_NUMBER + ", " + PARAM_COLUMN_NUMBER;

  static final String DEFAULT_METHOD_PARAMETER_REASSIGNED_MESSAGE_TEMPLATE = PARAM_VARIABLE_NAME + " was reassigned at line " + PARAM_LINE_NUMBER + ".";
  static final String METHOD_PARAMETER_REASSIGNED_MESSAGE_TEMPLATE_DESCRIPTION =
    "Message template that will be used to file an issue (method parameter reassignment). You can use following parameters " +
      PARAM_VARIABLE_NAME + ", " + PARAM_LINE_NUMBER + ", " + PARAM_COLUMN_NUMBER;

  static final String DEFAULT_FORBID_LOCAL_VARIABLE_REASSIGNMENT = TRUE;
  static final String FORBID_LOCAL_VARIABLE_REASSIGNMENT_DESCRIPTION = "File an issue when local variable is being reassigned unless it's annotated by configured annotation";

  static final String DEFAULT_FORBID_METHOD_PARAMETER_REASSIGNMENT = TRUE;
  static final String FORBID_METHOD_PARAMETER_REASSIGNMENT_DESCRIPTION = "File an issue when local variable / method parameter is being reassigned unless it's annotated by configured annotation";

  static final String DEFAULT_FORBID_REASSIGNMENT_INSIDE_LOOP = TRUE;
  static final String FORBID_REASSIGNMENT_INSIDE_LOOP_DESCRIPTION = "File an issue when local variable / method parameter is being assigned inside loop";

  static final String DEFAULT_MUTABLE_ANNOTATION_NAME = "Mutable";
  static final String MUTABLE_ANNOTATION_DESCRIPTION = "Annotation name (Class#getSimpleName) to be used on variables / method parameters that are allowed to be reassigned";

  public static final String KEYWORD_THIS = "this";
  public static Tree.Kind[] LOOP_TREE = { WHILE_STATEMENT, DO_STATEMENT, FOR_STATEMENT, FOR_EACH_STATEMENT };
  public static Tree.Kind[] BREAK_OUT_OF_SWITCH_EXPRESSION = { BREAK_STATEMENT, RETURN_STATEMENT };
  public static Tree.Kind[] HANDLED_UNARY_OPERATOR = { PREFIX_INCREMENT, PREFIX_DECREMENT, POSTFIX_DECREMENT, POSTFIX_INCREMENT };

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
