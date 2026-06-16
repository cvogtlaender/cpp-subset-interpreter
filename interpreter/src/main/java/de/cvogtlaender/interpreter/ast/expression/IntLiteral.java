package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

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

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitIntLiteral(this);
  }
}
