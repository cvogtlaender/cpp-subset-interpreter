package de.cvogtlaender.interpreter.ast.declaration;

import de.cvogtlaender.interpreter.ast.type.Type;

public class FieldDecl extends Decl {
  private Type type;
  private String name;

  public FieldDecl(Type type, String name) {
    this.type = type;
    this.name = name;
  }

  public Type getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toStringTree() {
    return "\"" + name + ":" + type.getName() + "\"";
  }
}
