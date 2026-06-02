package de.cvogtlaender.interpreter.ast.expression;

public class ErrorExpr extends Expr {

  private String message;

  public ErrorExpr(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toStringTree() {
    return "\"ErrorExpr\": " + message;
  }

}
