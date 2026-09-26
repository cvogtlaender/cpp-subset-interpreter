package de.cvogtlaender.interpreter.ast.expression;

import de.cvogtlaender.interpreter.ast.declaration.Decl;
import java.util.List;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class CallExpr extends Expr {

  private Expr callee;
  private List<Expr> arguments;

  public enum Kind {
    FUNCTION, METHOD, CONSTRUCTOR
  }

  private Kind kind;
  // FunctionDecl, MethodDecl or ConstructorDecl chosen by overload resolution
  private Decl target;

  public Kind getKind() {
    return kind;
  }

  public void setKind(Kind kind) {
    this.kind = kind;
  }

  public Decl getTarget() {
    return target;
  }

  public void setTarget(Decl target) {
    this.target = target;
  }

  public CallExpr(Expr callee, List<Expr> arguments) {
    this.callee = callee;
    this.arguments = arguments;
  }

  public Expr getCallee() {
    return callee;
  }

  public List<Expr> getArguments() {
    return arguments;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"CallExpr\": { \"Callee\": ");
    builder.append(this.callee.toStringTree());
    builder.append(", \"Arguments\": [ ");

    for (Expr arg : this.arguments) {
      builder.append(arg.toStringTree());
      builder.append(", ");
    }

    if (!this.arguments.isEmpty()) {
      builder.deleteCharAt(builder.lastIndexOf(","));
    }

    builder.append("]}");

    return builder.toString();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitCallExpr(this);
  }
}
