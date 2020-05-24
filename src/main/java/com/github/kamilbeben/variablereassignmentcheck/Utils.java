package com.github.kamilbeben.variablereassignmentcheck;

import org.sonar.plugins.java.api.tree.*;

public class Utils {

  private Utils() {}

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

  public static boolean isInsideLoop(Tree cursor) {
    return recursivelyGetParentLoopStatementTree(cursor) != null;
  }

  public static boolean isInsideLoopParenthesis(Tree cursor) {
    final Tree loopStatementTree = recursivelyGetParentLoopStatementTree(cursor);
    if (loopStatementTree == null) return false;

    final SyntaxToken openToken;
    final SyntaxToken closeToken;

    switch (loopStatementTree.kind()) {
      case WHILE_STATEMENT:
        openToken = ((WhileStatementTree) loopStatementTree).openParenToken();
        closeToken = ((WhileStatementTree) loopStatementTree).closeParenToken();
        break;
      case DO_STATEMENT:
        openToken = ((DoWhileStatementTree) loopStatementTree).openParenToken();
        closeToken = ((DoWhileStatementTree) loopStatementTree).closeParenToken();
        break;
      case FOR_STATEMENT:
        openToken = ((ForStatementTree) loopStatementTree).openParenToken();
        closeToken = ((ForStatementTree) loopStatementTree).closeParenToken();
        break;
      case FOR_EACH_STATEMENT:
        openToken = ((ForEachStatement) loopStatementTree).openParenToken();
        closeToken = ((ForEachStatement) loopStatementTree).closeParenToken();
        break;
      default:
        return false;
    }

    return isWithin(openToken, closeToken, cursor);
  }

  private static Tree recursivelyGetParentLoopStatementTree(Tree cursor) {
    for (
      Tree parent = cursor.parent();
      parent != null;
      parent = parent.parent()
    ) {
      if (parent.is(Constant.LOOP_TREE)) {
        return parent;
      }
    }
    return null;
  }
}
