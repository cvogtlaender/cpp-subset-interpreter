package de.cvogtlaender.interpreter.ast;

import de.cvogtlaender.interpreter.ast.type.Type;

public class Parameter {
  private Type type;
  private String name;

  public Parameter(Type type, String name) {
    this.type = type;
    this.name = name;
  }

  public Type getType() {
    return type;
  }

  public String getName() {
    return name;
  }

}
