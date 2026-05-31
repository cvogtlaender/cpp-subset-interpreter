package de.cvogtlaender.interpreter.ast;

import java.util.List;

import de.cvogtlaender.interpreter.ast.statement.Stmt;

public class ClassDef extends Stmt {

  private String className;
  private String parentClassName;

  private List<Parameter> fields;
  private List<Constructor> constructors;
  private List<MethodDef> methods;

  public ClassDef(String className, String parentClassName, List<Parameter> fields, List<Constructor> constructors,
      List<MethodDef> methods) {
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

  public List<Parameter> getFields() {
    return fields;
  }

  public List<Constructor> getConstructors() {
    return constructors;
  }

  public List<MethodDef> getMethods() {
    return methods;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"Class\": {\"Name\": \"" + this.className + "\"");
    builder.append(", \"ParentName\": \"" + this.parentClassName + "\"");
    builder.append(", \"Fields\": [");

    for (Parameter field : this.fields) {
      builder.append("\"" + field.getName() + ":" + field.getType().getName() + "\",");
    }

    if (!this.fields.isEmpty()) {
      builder.deleteCharAt(builder.lastIndexOf(","));
    }
    builder.append("]");
    builder.append(", \"Constructors\": [");

    for (Constructor constructor : this.constructors) {
      builder.append(constructor.toStringTree());
      builder.append(",");
    }

    if (!this.constructors.isEmpty()) {
      builder.deleteCharAt(builder.lastIndexOf(","));
    }

    builder.append("]");
    builder.append(", \"Methods\": [");

    for (MethodDef method : this.methods) {
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
}
