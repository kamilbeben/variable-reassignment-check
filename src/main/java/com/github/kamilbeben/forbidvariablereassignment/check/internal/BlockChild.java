package com.github.kamilbeben.forbidvariablereassignment.check.internal;

import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class BlockChild {

  private final SyntaxToken firstToken;
  private final SyntaxToken lastToken;
  private final Block parent;

  protected BlockChild(Block parent, SyntaxToken firstToken, SyntaxToken lastToken) {
    this.parent = parent;
    this.firstToken = firstToken;
    this.lastToken = lastToken;
  }

  public Block parent() {
    return parent;
  }

  public SyntaxToken firstToken() {
    return firstToken;
  }

  public SyntaxToken lastToken() {
    return lastToken;
  }

  public List<Block> ancestorsClosestToFurthest() {
    final List<Block> ancestors = new ArrayList<>();
    for (
      Block ancestor = parent();
      ancestor != null;
      ancestor = ancestor.parent()
    ) {
      ancestors.add(ancestor);
    }
    return ancestors;
  }

  public boolean contains(Tree cursor) {
    final boolean startsBeforeCursor =
      firstToken.line() < cursor.firstToken().line() ||
      (
        firstToken.line() == cursor.firstToken().line() &&
        firstToken.column() <= cursor.firstToken().column()
      );

    final boolean endsAfterCursor =
      lastToken.line() > cursor.lastToken().line() ||
      (
        lastToken.line() == cursor.lastToken().line() &&
        lastToken.column() >= cursor.lastToken().column()
      );

    return startsBeforeCursor && endsAfterCursor;
  }

  public boolean startsAt(SyntaxToken token) {
    return
      Objects.equals(firstToken.line(), token.line()) &&
      Objects.equals(firstToken.column(), token.column());
  }

  public boolean endsAt(SyntaxToken token) {
    return
      Objects.equals(lastToken.line(), token.line()) &&
      Objects.equals(lastToken.column(), token.column());
  }
}
