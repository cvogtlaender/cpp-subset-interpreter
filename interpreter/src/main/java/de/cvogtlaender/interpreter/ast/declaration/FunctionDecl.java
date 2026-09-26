package de.cvogtlaender.interpreter.ast.declaration;

import java.util.List;

import de.cvogtlaender.interpreter.ast.statement.BlockStmt;
import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class FunctionDecl extends Decl {
  private Type returnType;
  private String name;
  private List<ParameterDecl> parameters;
  private BlockStmt body;
  private boolean builtin;

  public static FunctionDecl builtin(Type returnType, String name, List<ParameterDecl> parameters) {
    FunctionDecl f = new FunctionDecl(returnType, name, parameters, new BlockStmt(new java.util.ArrayList<>()));
    f.builtin = true;
    return f;
  }

  public boolean isBuiltin() {
    return builtin;
  }

  public FunctionDecl(Type returnType, String name, List<ParameterDecl> parameters, BlockStmt body) {
    this.returnType = returnType;
    this.name = name;
    this.parameters = parameters;
    this.body = body;
  }

  public Type getReturnType() {
    return returnType;
  }

  public String getName() {
    return name;
  }

  public List<ParameterDecl> getParameters() {
    return parameters;
  }

  public BlockStmt getBody() {
    return body;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"Func\": {\"Name\": \"" + this.name + "\"");
    builder.append(", \"ReturnType\": \"" + this.returnType.getName() + "\"");
    builder.append(", \"Parameters\": [");

    for (ParameterDecl parameter : this.parameters) {
      builder.append(parameter.toStringTree());
    }

    if (!this.parameters.isEmpty()) {
      builder.deleteCharAt(builder.lastIndexOf(","));
    }

    builder.append("], \"Body\": ");
    builder.append(this.body.toStringTree());
    builder.append("}");

    return builder.toString();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitFunctionDecl(this);
  }
}
