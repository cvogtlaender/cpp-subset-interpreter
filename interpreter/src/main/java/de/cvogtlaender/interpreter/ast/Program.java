package de.cvogtlaender.interpreter.ast;

import java.util.List;

public class Program extends Node {
  private List<FunctionDef> functions;
  private List<ClassDef> classDefs;

  public Program(List<FunctionDef> functions, List<ClassDef> classDefs) {
    this.functions = functions;
    this.classDefs = classDefs;
  }

  public List<FunctionDef> getFunctions() {
    return functions;
  }

  public List<ClassDef> getClassDefs() {
    return classDefs;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("{");

    for (ClassDef classDef : this.classDefs) {
      builder.append(classDef.toStringTree());
      builder.append(",");
    }

    for (FunctionDef func : this.functions) {
      builder.append(func.toStringTree());
      builder.append(",");
    }

    if (!this.functions.isEmpty()) {
      builder.deleteCharAt(builder.lastIndexOf(","));
    }

    builder.append("}");

    return builder.toString();
  }
}
