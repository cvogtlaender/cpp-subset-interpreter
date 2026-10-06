package de.cvogtlaender.interpreter.ast.expression;

import java.util.List;

import de.cvogtlaender.interpreter.ast.declaration.ConstructorDecl;
import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class NewExpr extends Expr {

  private Type allocatedType;
  private List<Expr> arguments;
  private ConstructorDecl constructor;

  public NewExpr(Type allocatedType, List<Expr> arguments) {
    this.allocatedType = allocatedType;
    this.arguments = arguments;
  }

  public Type getAllocatedType() {
    return allocatedType;
  }

  public List<Expr> getArguments() {
    return arguments;
  }

  public ConstructorDecl getConstructor() {
    return constructor;
  }

  public void setConstructor(ConstructorDecl constructor) {
    this.constructor = constructor;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"NewExpr\": { \"Type\": \"" + this.allocatedType.getName() + "\", \"Arguments\": [ ");

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
    return visitor.visitNewExpr(this);
  }
}
