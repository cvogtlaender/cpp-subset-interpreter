package de.cvogtlaender.interpreter.ast.declaration;

import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class FieldDecl extends Decl {
  private Type type;
  private String name;
  private ClassDecl owner;

  public ClassDecl getOwner() {
    return owner;
  }

  public void setOwner(ClassDecl owner) {
    this.owner = owner;
  }

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

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitFieldDecl(this);
  }
}
