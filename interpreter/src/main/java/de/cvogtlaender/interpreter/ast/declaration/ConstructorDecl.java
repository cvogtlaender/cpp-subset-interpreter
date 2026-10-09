package de.cvogtlaender.interpreter.ast.declaration;

import java.util.List;

import de.cvogtlaender.interpreter.ast.statement.BlockStmt;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class ConstructorDecl extends Decl {

  private String name;
  private List<ParameterDecl> parameters;
  private BlockStmt body;
  private ClassDecl owner;

  public ClassDecl getOwner() {
    return owner;
  }

  public void setOwner(ClassDecl owner) {
    this.owner = owner;
  }

  private boolean synthesized;

  public boolean isSynthesized() {
    return synthesized;
  }

  public void setSynthesized(boolean synthesized) {
    this.synthesized = synthesized;
  }

  public ConstructorDecl(String name, List<ParameterDecl> parameters, BlockStmt body) {
    this.name = name;
    this.parameters = parameters;
    this.body = body;
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
    builder.append("\"Constructor\": {\"Name\": \"" + this.name + "\"");
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
    return visitor.visitConstructorDecl(this);
  }
}
