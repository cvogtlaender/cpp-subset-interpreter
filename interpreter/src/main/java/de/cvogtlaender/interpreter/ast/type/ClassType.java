package de.cvogtlaender.interpreter.ast.type;

public class ClassType extends Type {
  private String name;

  public ClassType(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return this.name;
  }
}
