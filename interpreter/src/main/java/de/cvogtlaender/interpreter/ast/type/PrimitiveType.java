package de.cvogtlaender.interpreter.ast.type;

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
}
