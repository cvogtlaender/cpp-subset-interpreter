package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

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

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitBoolLiteral(this);
  }
}
