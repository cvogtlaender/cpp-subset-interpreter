package de.cvogtlaender.interpreter.ast.declaration;

import java.util.List;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class ClassDecl extends Decl {

  private String className;
  private String parentClassName;

  private List<FieldDecl> fields;
  private List<ConstructorDecl> constructors;
  private List<MethodDecl> methods;

  public ClassDecl(String className, String parentClassName, List<FieldDecl> fields,
      List<ConstructorDecl> constructors,
      List<MethodDecl> methods) {
    this.className = className;
    this.parentClassName = parentClassName;
    this.fields = fields;
    this.constructors = constructors;
    this.methods = methods;
  }

  public String getClassName() {
    return className;
  }

  public String getParentClassName() {
    return parentClassName;
  }

  public List<FieldDecl> getFields() {
    return fields;
  }

  public List<ConstructorDecl> getConstructors() {
    return constructors;
  }

  public List<MethodDecl> getMethods() {
    return methods;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"Class\": {\"Name\": \"" + this.className + "\"");
    builder.append(", \"ParentName\": \"" + this.parentClassName + "\"");
    builder.append(", \"Fields\": [");

    for (FieldDecl field : this.fields) {
      builder.append(field.toStringTree());
      builder.append(",");
    }

    if (!this.fields.isEmpty()) {
      builder.deleteCharAt(builder.lastIndexOf(","));
    }
    builder.append("]");
    builder.append(", \"Constructors\": [");

    for (ConstructorDecl constructor : this.constructors) {
      builder.append(constructor.toStringTree());
      builder.append(",");
    }

    if (!this.constructors.isEmpty()) {
      builder.deleteCharAt(builder.lastIndexOf(","));
    }

    builder.append("]");
    builder.append(", \"Methods\": [");

    for (MethodDecl method : this.methods) {
      builder.append(method.toStringTree());
      builder.append(",");
    }

    if (!this.methods.isEmpty()) {
      builder.deleteCharAt(builder.lastIndexOf(","));
    }

    builder.append("]");
    builder.append("}");

    return builder.toString();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitClassDecl(this);
  }
}
