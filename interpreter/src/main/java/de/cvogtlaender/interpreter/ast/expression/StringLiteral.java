package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

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

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitStringLiteral(this);
  }
}
