package de.cvogtlaender.interpreter.ast;

import java.util.List;

import de.cvogtlaender.interpreter.ast.statement.BlockStmt;
import de.cvogtlaender.interpreter.ast.statement.Stmt;
import de.cvogtlaender.interpreter.ast.type.Type;

public class MethodDef extends Stmt {

  private Type returnType;
  private String name;
  private List<Parameter> parameters;
  private BlockStmt body;
  private boolean isVirtual;

  public MethodDef(Type returnType, String name, List<Parameter> parameters, BlockStmt body, boolean isVirtual) {
    this.returnType = returnType;
    this.name = name;
    this.parameters = parameters;
    this.body = body;
    this.isVirtual = isVirtual;
  }

  public Type getReturnType() {
    return returnType;
  }

  public String getName() {
    return name;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  public BlockStmt getBody() {
    return body;
  }

  public boolean getIsVirtual() {
    return isVirtual;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"Method\": {\"Name\": \"" + this.name + "\"");
    builder.append("\"Virtual\": " + Boolean.toString(this.isVirtual) + ", ");
    builder.append(", \"ReturnType\": \"" + this.returnType.getName() + "\"");
    builder.append(", \"Parameters\": [");

    for (Parameter parameter : this.parameters) {
      builder.append("\"" + parameter.getName() + ":" + parameter.getType().getName() + "\",");
    }

    if (!this.parameters.isEmpty()) {
      builder.deleteCharAt(builder.lastIndexOf(","));
    }

    builder.append("], \"Body\": ");
    builder.append(this.body.toStringTree());
    builder.append("}");

    return builder.toString();
  }
}
