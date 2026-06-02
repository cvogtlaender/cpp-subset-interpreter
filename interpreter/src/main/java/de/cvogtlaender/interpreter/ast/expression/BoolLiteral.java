package de.cvogtlaender.interpreter.ast.expression;

public class BoolLiteral extends LiteralExpr {

  private Boolean value;

  public BoolLiteral(Boolean value) {
    this.value = value;
  }

  public Boolean getValue() {
    return value;
  }

  @Override
  public String toStringTree() {
    return "\"BoolLit\": " + value.toString();
  }
}
