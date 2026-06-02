package de.cvogtlaender.interpreter.ast.statement;

import de.cvogtlaender.interpreter.ast.declaration.VariableDecl;

public class VariableStmt extends Stmt {

  private VariableDecl variableDecl;

  public VariableStmt(VariableDecl variableDecl) {
    this.variableDecl = variableDecl;
  }

  public VariableDecl getVariableDecl() {
    return variableDecl;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"VarStmt\": {");
    builder.append(variableDecl.toStringTree());
    builder.append("}");
    return builder.toString();
  }
}
