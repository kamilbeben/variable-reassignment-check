package com.github.kamilbeben.variablereassignmentcheck;

import org.sonar.plugins.java.api.tree.Tree;

import static org.sonar.plugins.java.api.tree.Tree.Kind.*;

public class Constant {

  // logic related
  public static final String KEYWORD_THIS = "this";
  public static Tree.Kind[] LOOP_TREE = { WHILE_STATEMENT, DO_STATEMENT, FOR_STATEMENT, FOR_EACH_STATEMENT };
  public static Tree.Kind[] BREAK_OUT_OF_SWITCH_EXPRESSION = { BREAK_STATEMENT, RETURN_STATEMENT };
  public static Tree.Kind[] HANDLED_UNARY_OPERATOR = { PREFIX_INCREMENT, PREFIX_DECREMENT, POSTFIX_DECREMENT, POSTFIX_INCREMENT };

  static class Check {
    static final String KEY = "variable-reassignment";
    static final String NAME = "Variable should not be reassigned";
    // @formatter:off
    static final String DESCRIPTION =
      "<p>" +
        "Reassigning variables (oh, the irony) adds unnecessary layer of complexity to the code and, " +
        "in most cases it means that the code simply could be better (for example, some parts could be extracted to another method)" +
      "</p>" +
      "<p>" +
        "<h1>Noncompliant example</h1>" +
        "<pre>" +
          "void foo(Bar bar) {\n" +
          "  String barName = bar.getName();\n" +
          "  if (barName == null) {\n" +
          "    barName = \"defaultName\"; // Noncompliant\n" +
          "  }\n" +
          "}" +
        "</pre>" +
      "</p>" +
      "<p>" +
        "<h1> Compliant solution example 1</h1>" +
        "<pre>" +
          "void foo(Bar bar) {\n" +
          "  String barName = Optional.ofNullable(bar.getName()).orElse(\"defaultName\");\n" +
          "}" +
        "</pre>" +
      "</p>" +
      "<p>" +
        "<h1>Exceptions</h1>" +
        "<ul>" +
          "<li> Variables defined in loop's parenthesis, for example <pre>for (int i=0; i < size; i++) {}</pre> </li>" +
          "<li> Class members </li>" +
        "</ul>" +
      "</p>";
    // @formatter:off
  }

  static class Parameter {

    static final String TRUE = "true";

    static class MessageTemplate {
      static final String PARAM_VARIABLE_NAME = "{variableName}";
      static final String DEFAULT = "Local variable " + PARAM_VARIABLE_NAME + " was reassigned.";
      static final String DESCRIPTION = "Message template that will be used to file an issue (variable reassignment). You can use " + PARAM_VARIABLE_NAME + " parameter.";
    }

    static class ReportMethodParameterReassignment {
      static final String DEFAULT = TRUE;
      static final String DESCRIPTION = "File an issue when local variable / method parameter is being assigned inside loop";
    }

    static class ReportLocalVariableReassignment {
      static final String DEFAULT = TRUE;
      static final String DESCRIPTION = "File an issue when local variable is being reassigned unless it's annotated by configured annotation";
    }

    static class ReportReassignmentInsideLoop {
      static final String DEFAULT = TRUE;
      static final String DESCRIPTION = "File an issue when local variable / method parameter is being reassigned unless it's annotated by configured annotation";
    }

    static class MutableAnnotationName {
      static final String DEFAULT = "Mutable";
      static final String DESCRIPTION = "Annotation name (without package info and <i>@</i>) to be used on variables / method parameters that are allowed to be reassigned";
    }
  }
}
