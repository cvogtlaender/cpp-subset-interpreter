package de.cvogtlaender.interpreter.ast;

import java.util.List;

import de.cvogtlaender.interpreter.ast.expression.Expr;

/**
 * One chunk of REPL input: class/function declarations and statements in
 * source order, optionally followed by a bare expression whose value is shown.
 * Not an AstNode itself; it only groups top-level nodes.
 */
public record ReplInput(List<AstNode> items, Expr trailingExpr) {
}
