package de.cvogtlaender.interpreter.ast.expression;

public class CharLiteral extends LiteralExpr {

  private Character value;

  public CharLiteral(Character value) {
    this.value = value;
  }

  public Character getValue() {
    return value;
  }

  @Override
  public String toStringTree() {
    return "\"CharLit\": \"" + value + "\"";
  }

}
