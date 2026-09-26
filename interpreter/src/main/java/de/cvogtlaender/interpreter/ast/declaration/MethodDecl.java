package de.cvogtlaender.interpreter.ast.declaration;

import java.util.List;

import de.cvogtlaender.interpreter.ast.statement.BlockStmt;
import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class MethodDecl extends Decl {

  private Type returnType;
  private String name;
  private List<ParameterDecl> parameters;
  private BlockStmt body;
  private boolean isVirtual;
  private ClassDecl owner;

  public ClassDecl getOwner() {
    return owner;
  }

  public void setOwner(ClassDecl owner) {
    this.owner = owner;
  }

  // virtual either explicitly or by overriding a virtual base method
  private boolean effectivelyVirtual;

  public boolean isEffectivelyVirtual() {
    return effectivelyVirtual;
  }

  public void setEffectivelyVirtual(boolean effectivelyVirtual) {
    this.effectivelyVirtual = effectivelyVirtual;
  }

  public MethodDecl(Type returnType, String name, List<ParameterDecl> parameters, BlockStmt body, boolean isVirtual) {
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

  public List<ParameterDecl> getParameters() {
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
    builder.append(", \"Virtual\": " + Boolean.toString(this.isVirtual) + ", ");
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
    return visitor.visitMethodDecl(this);
  }
}
