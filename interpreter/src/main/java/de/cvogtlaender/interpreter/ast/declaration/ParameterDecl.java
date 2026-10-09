package de.cvogtlaender.interpreter.ast.declaration;

import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class ParameterDecl extends Decl {
  private Type type;
  private String name;

  public ParameterDecl(Type type, String name) {
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

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitParameterDecl(this);
  }
}
