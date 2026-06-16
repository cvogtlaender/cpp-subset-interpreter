package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.ast.declaration.Decl;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class VarExpr extends Expr {

  private String name;
  private Decl resolvedDecl;

  public VarExpr(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public Decl getResolvedDecl() {
    return resolvedDecl;
  }

  public void setResolvedDecl(Decl resolvedDecl) {
    this.resolvedDecl = resolvedDecl;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"VarExpr\": \"" + this.name + "\"");
    return builder.toString();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitVarExpr(this);
  }
}
