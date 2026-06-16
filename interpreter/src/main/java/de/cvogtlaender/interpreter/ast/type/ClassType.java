package de.cvogtlaender.interpreter.ast.type;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class ClassType extends Type {
  private String name;

  public ClassType(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitClassType(this);
  }
}
