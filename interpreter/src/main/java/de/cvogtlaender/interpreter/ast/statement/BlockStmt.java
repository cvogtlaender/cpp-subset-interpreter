package de.cvogtlaender.interpreter.ast.statement;

import java.util.List;

import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class BlockStmt extends Stmt {
  private List<Stmt> statements;

  public BlockStmt(List<Stmt> statements) {
    this.statements = statements;
  }

  public List<Stmt> getStatements() {
    return this.statements;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"BlockStmt\": {");

    for (Stmt stmt : this.statements) {
      builder.append(stmt.toStringTree());
      builder.append(",");
    }

    if (!this.statements.isEmpty()) {
      builder.deleteCharAt(builder.lastIndexOf(","));
    }

    builder.append("}");

    return builder.toString();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitBlockStmt(this);
  }
}
