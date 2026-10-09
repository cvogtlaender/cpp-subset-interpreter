package de.cvogtlaender.interpreter.ast;

import java.util.List;

import de.cvogtlaender.interpreter.ast.expression.Expr;

public record ReplInput(List<AstNode> items, Expr trailingExpr) {
}
