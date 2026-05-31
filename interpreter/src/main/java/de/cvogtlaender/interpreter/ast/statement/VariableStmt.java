package de.cvogtlaender.interpreter.ast.statement;

import de.cvogtlaender.interpreter.ast.expression.Expr;
import de.cvogtlaender.interpreter.ast.type.Type;

public class VariableStmt extends Stmt {
  private Type type;
  private String name;
  private Expr value;

  public VariableStmt(Type type, String name, Expr value) {
    this.type = type;
    this.name = name;
    this.value = value;
  }

  public VariableStmt(Type type, String name) {
    this.type = type;
    this.name = name;
  }

  public Type getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Expr getValue() {
    return value;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"VarStmt\": {  \"Type\": \"");
    builder.append(type.getName() + "\"");
    builder.append(", \"Name\": \"" + this.name + "\"");
    builder.append(", \"Value\": ");
    builder.append(value == null ? "\"null\"" : value.toStringTree());
    builder.append("}");
    return builder.toString();
  }

}
