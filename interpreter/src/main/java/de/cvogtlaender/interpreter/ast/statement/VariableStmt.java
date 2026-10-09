package de.cvogtlaender.interpreter.ast.statement;

import de.cvogtlaender.interpreter.ast.declaration.VariableDecl;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

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

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitVariableStmt(this);
  }
}
