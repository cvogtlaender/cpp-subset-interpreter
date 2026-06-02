package de.cvogtlaender.interpreter.ast.expression;

public class VarExpr extends Expr {

  private String name;

  public VarExpr(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"VarExpr\": \"" + this.name + "\"");
    return builder.toString();
  }

}
