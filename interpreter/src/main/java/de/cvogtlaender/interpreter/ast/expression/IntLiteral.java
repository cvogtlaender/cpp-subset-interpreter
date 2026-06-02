package de.cvogtlaender.interpreter.ast.expression;

public class IntLiteral extends LiteralExpr {

  private Integer value;

  public IntLiteral(Integer value) {
    this.value = value;
  }

  public Integer getValue() {
    return value;
  }

  @Override
  public String toStringTree() {
    return "\"IntLit\": " + value;
  }

}
