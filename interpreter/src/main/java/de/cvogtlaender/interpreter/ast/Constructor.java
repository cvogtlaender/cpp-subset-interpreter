package de.cvogtlaender.interpreter.ast;

import java.util.List;

import de.cvogtlaender.interpreter.ast.statement.BlockStmt;
import de.cvogtlaender.interpreter.ast.statement.Stmt;

public class Constructor extends Stmt {

  private String name;
  private List<Parameter> parameters;
  private BlockStmt body;

  public Constructor(String name, List<Parameter> parameters, BlockStmt body) {
    this.name = name;
    this.parameters = parameters;
    this.body = body;
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

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"Constructor\": {\"Name\": \"" + this.name + "\"");
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
