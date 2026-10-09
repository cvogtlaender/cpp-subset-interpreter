package de.cvogtlaender.interpreter.ast.statement;

import de.cvogtlaender.interpreter.ast.expression.Expr;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

public class WhileStmt extends Stmt {
  private Expr condition;
  private Stmt body;

  public WhileStmt(Expr condition, Stmt body) {
    this.condition = condition;
    this.body = body;
  }

  public Expr getCondition() {
    return condition;
  }

  public Stmt getBody() {
    return body;
  }

  @Override
  public String toStringTree() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"WhileStmt\": { \"Cond\": ");
    builder.append(this.condition == null ? "\"null\"" : this.condition.toStringTree());
    builder.append(", \"Body\": ");
    builder.append(this.body == null ? "\"null\"" : this.body.toStringTree());
    builder.append("}");
    return builder.toString();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitWhileStmt(this);
  }
}
