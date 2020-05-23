package com.github.kamilbeben.forbidvariablereassignment.check.internal;

import com.google.common.collect.ImmutableList;
import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.kamilbeben.forbidvariablereassignment.check.internal.Block.Type.MUTUALLY_EXCLUSIVE_STATEMENTS_WRAPPER;

public class Block extends BlockChild {

  public enum Type {
    INEVITABLE,
    CONDITIONAL,
    /**
     * Contains blocks which are mutually excluding each other
     * (eg [ IF, ELSEIF, ELSE ], or [ CASE, CASE, DEFAULT ])
     */
    MUTUALLY_EXCLUSIVE_STATEMENTS_WRAPPER
  }

  private final Type type;
  private final List<BlockChild> children = new ArrayList<>();

  protected Block(Block parent, SyntaxToken firstToken, SyntaxToken lastToken, Type type) {
    super(parent, firstToken, lastToken);
    this.type = type;

    if (parent != null) {
      parent.addChild(this);
    }
  }

  public static Block create(Block parent, Tree tree, Type type) {
    return new Block(parent, tree.firstToken(), tree.lastToken(), type);
  }

  public static Block create(Block parent, SyntaxToken firstToken, SyntaxToken lastToken, Type type) {
    return new Block(parent, firstToken, lastToken, type);
  }

  public Type type() {
    return type;
  }

  public List<BlockChild> children() {
    return ImmutableList.copyOf(children);
  }

  public Variable findVariable(String name, Tree cursor) {
    for (
      Block block = nearestBlock(cursor);
      block != null;
      block = block.parent()
    ) {

      final Variable variable = block.children().stream()
        .filter(Variable.class::isInstance)
        .map(Variable.class::cast)
        .filter(it -> it.name().equals(name))
        .findAny()
        .orElse(null);

      if (variable != null) {
        return variable;
      }
    }
    return null;
  }

  public Block nearestBlock(Tree cursor) {
    return allDescendantBlocks().stream()
      .filter(block ->
        block.type() != MUTUALLY_EXCLUSIVE_STATEMENTS_WRAPPER &&
          block.contains(cursor)
      )
      .sorted(
        (a, b) -> {
          final int aLine = a.firstToken().line();
          final int bLine = b.firstToken().line();

          if (aLine < bLine) return 1;
          if (aLine > bLine) return -1;

          final int aColumn = a.firstToken().column();
          final int bColumn = b.firstToken().column();

          if (aColumn < bColumn) return 1;
          if (aColumn > bColumn) return -1;
          return 0;
        }
      )
      .findFirst()
      .orElse(null);
  }

  public List<Block> allDescendantBlocks() {
    return allDescendantBlocks(this);
  }

  private List<Block> allDescendantBlocks(BlockChild blockChild) {
    return blockChild instanceof Block
      ? new ImmutableList.Builder<Block>()
      .add((Block) blockChild)
      .addAll(
        ((Block) blockChild).children().stream()
          .map(this::allDescendantBlocks).flatMap(List::stream)
          .collect(Collectors.toList())
      )
      .build()
      : Collections.emptyList();
  }

  void addChild(BlockChild child) {
    children.add(child);
  }
}
