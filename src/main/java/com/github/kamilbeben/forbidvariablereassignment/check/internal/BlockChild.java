package com.github.kamilbeben.forbidvariablereassignment.check.internal;

import com.github.kamilbeben.forbidvariablereassignment.check.Utils;
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
    return Utils.isWithin(firstToken, lastToken, cursor);
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
