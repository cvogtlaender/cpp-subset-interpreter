package de.cvogtlaender.interpreter.ast.expression;

public class StringLiteral extends LiteralExpr {

  private String value;

  public StringLiteral(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toStringTree() {
    return "\"StringLit\":" + value;
  }

}
