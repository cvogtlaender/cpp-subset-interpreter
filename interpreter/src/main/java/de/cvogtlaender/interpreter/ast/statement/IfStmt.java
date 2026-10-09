package de.cvogtlaender.interpreter.ast.statement;

import de.cvogtlaender.interpreter.ast.expression.Expr;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class IfStmt extends Stmt {
  private Expr condition;
  private Stmt ifBranch;
  private Stmt elseBranch;

  public IfStmt(Expr condition, Stmt ifBranch, Stmt elseBranch) {
    this.condition = condition;
    this.ifBranch = ifBranch;
    this.elseBranch = elseBranch;
  }

  public Expr getCondition() {
    return condition;
  }

  public Stmt getIfBranch() {
    return ifBranch;
  }

  public Stmt getElseBranch() {
    return elseBranch;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"IfStmt\": { \"Cond\": ");
    builder.append(this.condition == null ? "\"null\"" : this.condition.toStringTree());
    builder.append(", \"IfBranch\": ");
    builder.append(this.ifBranch == null ? "\"null\"" : this.ifBranch.toStringTree());
    builder.append(", \"ElseBranch\": ");
    builder.append(this.elseBranch == null ? "\"null\"" : this.elseBranch.toStringTree());
    builder.append("}");
    return builder.toString();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitIfStmt(this);
  }
}
