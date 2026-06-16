package de.cvogtlaender.interpreter.ast.type;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public abstract class Type {

  public abstract String getName();

  public abstract <T> T accept(AstVisitor<T> visitor);
}
