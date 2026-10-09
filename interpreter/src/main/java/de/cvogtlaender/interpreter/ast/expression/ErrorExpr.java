package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

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

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitErrorExpr(this);
  }
}
