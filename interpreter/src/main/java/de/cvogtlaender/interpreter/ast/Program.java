package de.cvogtlaender.interpreter.ast;

import java.util.List;

import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.FunctionDecl;

public class Program extends AstNode {
  private List<FunctionDecl> functions;
  private List<ClassDecl> classDefs;

  public Program(List<FunctionDecl> functions, List<ClassDecl> classDefs) {
    this.functions = functions;
    this.classDefs = classDefs;
  }

  public List<FunctionDecl> getFunctions() {
    return functions;
  }

  public List<ClassDecl> getClassDefs() {
    return classDefs;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("{");

    for (ClassDecl classDef : this.classDefs) {
      builder.append(classDef.toStringTree());
      builder.append(",");
    }

    for (FunctionDecl func : this.functions) {
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
