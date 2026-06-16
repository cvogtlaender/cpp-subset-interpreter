package de.cvogtlaender.interpreter.ast.declaration;

import de.cvogtlaender.interpreter.ast.expression.Expr;
import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class VariableDecl extends Decl {

  private String name;
  private Type type;
  private Expr initializer;

  public VariableDecl(String name, Type type, Expr initializer) {
    this.name = name;
    this.type = type;
    this.initializer = initializer;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public Expr getInitializer() {
    return initializer;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"VarDecl\": {  \"Type\": \"");
    builder.append(type.getName() + "\"");
    builder.append(", \"Name\": \"" + this.name + "\"");
    builder.append(", \"Initializer\": ");
    builder.append(initializer == null ? "\"null\"" : initializer.toStringTree());
    builder.append("}");
    return builder.toString();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitVariableDecl(this);
  }
}
