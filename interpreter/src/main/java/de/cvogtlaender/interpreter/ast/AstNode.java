package de.cvogtlaender.interpreter.ast;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public abstract class AstNode {
  public abstract String toStringTree();

  public abstract <T> T accept(AstVisitor<T> visitor);
}
