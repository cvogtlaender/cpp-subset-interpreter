package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class MemberAccessExpr extends Expr {

  private Expr obj;
  private String memberName;

  public MemberAccessExpr(Expr obj, String memberName) {
    this.obj = obj;
    this.memberName = memberName;
  }

  public Expr getObj() {
    return obj;
  }

  public String getMemberName() {
    return memberName;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"MemberAccess\" : { \"Name\": \"" + this.memberName + "\",");
    builder.append(" \"Obj\": ");
    builder.append(this.obj.toStringTree());
    builder.append("}");
    return builder.toString();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitMemberAccessExpr(this);
  }
}
