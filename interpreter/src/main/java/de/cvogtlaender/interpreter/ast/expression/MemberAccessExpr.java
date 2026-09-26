package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.ast.declaration.FieldDecl;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class MemberAccessExpr extends Expr {

  private Expr obj;
  private String memberName;
  // 'p->m' instead of 'obj.m'
  private boolean arrow;
  private FieldDecl resolvedField;

  public FieldDecl getResolvedField() {
    return resolvedField;
  }

  public void setResolvedField(FieldDecl resolvedField) {
    this.resolvedField = resolvedField;
  }

  public MemberAccessExpr(Expr obj, String memberName, boolean arrow) {
    this.obj = obj;
    this.memberName = memberName;
    this.arrow = arrow;
  }

  public boolean isArrow() {
    return arrow;
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
    builder.append("\"MemberAccess\" : { \"Name\": \"" + this.memberName + "\", \"Arrow\": " + this.arrow + ",");
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
