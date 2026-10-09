package de.cvogtlaender.interpreter.ast.type;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class PrimitiveType extends Type {
  public enum Kind {
    INT, BOOL, CHAR, STRING, VOID
  }

  private Kind kind;

  public PrimitiveType(Kind kind) {
    this.kind = kind;
  }

  public Kind getKind() {
    return this.kind;
  }

  @Override
  public String getName() {
    return this.kind.name().toLowerCase();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitPrimitiveType(this);
  }
}
