package de.cvogtlaender.lsp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import de.cvogtlaender.interpreter.ast.AstNode;
import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.ConstructorDecl;
import de.cvogtlaender.interpreter.ast.declaration.Decl;
import de.cvogtlaender.interpreter.ast.declaration.FunctionDecl;
import de.cvogtlaender.interpreter.ast.declaration.MethodDecl;
import de.cvogtlaender.interpreter.ast.declaration.ParameterDecl;
import de.cvogtlaender.interpreter.ast.declaration.VariableDecl;
import de.cvogtlaender.interpreter.ast.expression.AssignExpr;
import de.cvogtlaender.interpreter.ast.expression.BinaryExpr;
import de.cvogtlaender.interpreter.ast.expression.CallExpr;
import de.cvogtlaender.interpreter.ast.expression.Expr;
import de.cvogtlaender.interpreter.ast.expression.MemberAccessExpr;
import de.cvogtlaender.interpreter.ast.expression.NewExpr;
import de.cvogtlaender.interpreter.ast.expression.UnaryExpr;
import de.cvogtlaender.interpreter.ast.statement.BlockStmt;
import de.cvogtlaender.interpreter.ast.statement.DeleteStmt;
import de.cvogtlaender.interpreter.ast.statement.ExprStmt;
import de.cvogtlaender.interpreter.ast.statement.IfStmt;
import de.cvogtlaender.interpreter.ast.statement.ReturnStmt;
import de.cvogtlaender.interpreter.ast.statement.Stmt;
import de.cvogtlaender.interpreter.ast.statement.VariableStmt;
import de.cvogtlaender.interpreter.ast.statement.WhileStmt;

/** Generic AST traversal: children in source order, lookup by position and scopes. */
public final class AstNodes {

  private static final Comparator<AstNode> BY_POSITION = Comparator.comparingInt(AstNode::getLine)
      .thenComparingInt(AstNode::getColumn);

  private AstNodes() {
  }

  /** Direct children of a node in source order; synthesized constructors are left out. */
  public static List<AstNode> children(AstNode node) {
    List<AstNode> result = switch (node) {
      case Program p -> concat(p.getClassDefs().stream(), p.getFunctions().stream());
      case ClassDecl c -> concat(c.getFields().stream(),
          c.getConstructors().stream().filter(k -> !k.isSynthesized()), c.getMethods().stream());
      case FunctionDecl f -> concat(f.getParameters().stream(), Stream.of(f.getBody()));
      case MethodDecl m -> concat(m.getParameters().stream(), Stream.of(m.getBody()));
      case ConstructorDecl k -> concat(k.getParameters().stream(), Stream.of(k.getBody()));
      case BlockStmt b -> concat(b.getStatements().stream());
      case VariableStmt s -> concat(Stream.of(s.getVariableDecl()));
      case VariableDecl d -> concat(Stream.of(d.getInitializer()));
      case IfStmt s -> concat(Stream.of(s.getCondition(), s.getIfBranch(), s.getElseBranch()));
      case WhileStmt s -> concat(Stream.of(s.getCondition(), s.getBody()));
      case ReturnStmt s -> concat(Stream.of(s.getReturnValue()));
      case DeleteStmt s -> concat(Stream.of(s.getPointer()));
      case ExprStmt s -> concat(Stream.of(s.getExpression()));
      case AssignExpr e -> concat(Stream.of(e.getTarget(), e.getValue()));
      case BinaryExpr e -> concat(Stream.of(e.getLeftHandSide(), e.getRightHandSide()));
      case UnaryExpr e -> concat(Stream.of(e.getExpr()));
      case CallExpr e -> concat(Stream.of(e.getCallee()), e.getArguments().stream());
      case MemberAccessExpr e -> concat(Stream.of(e.getObj()));
      case NewExpr e -> concat(e.getArguments().stream());
      default -> new ArrayList<>();
    };
    result.sort(BY_POSITION);
    return result;
  }

  @SafeVarargs
  private static List<AstNode> concat(Stream<? extends AstNode>... streams) {
    return Stream.of(streams).flatMap(s -> s).filter(Objects::nonNull).map(AstNode.class::cast)
        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
  }

  public static boolean contains(SourceText text, AstNode node, int offset) {
    return text.startOffset(node) <= offset && offset <= text.endOffset(node);
  }

  /** The innermost expression containing {@code offset}, or null. */
  public static Expr innermostExpr(Program program, SourceText text, int offset) {
    Expr found = null;
    AstNode node = program;
    while (node != null) {
      if (node instanceof Expr e) {
        found = e;
      }
      AstNode next = null;
      for (AstNode child : children(node)) {
        if (text.startOffset(child) <= offset && offset < text.endOffset(child)) {
          next = child;
          break;
        }
      }
      node = next;
    }
    return found;
  }

  /**
   * What is visible at an offset: the enclosing class and callable (either may
   * be null) and the parameters and local variables declared before it, in
   * declaration order.
   */
  public record Scope(ClassDecl enclosingClass, Decl callable, List<Decl> locals) {

    /** The innermost local variable or parameter named {@code name}, or null. */
    public Decl local(String name) {
      for (int i = locals.size() - 1; i >= 0; i--) {
        if (Names.of(locals.get(i)).equals(name)) {
          return locals.get(i);
        }
      }
      return null;
    }
  }

  public static Scope scopeAt(Program program, SourceText text, int offset) {
    for (FunctionDecl f : program.getFunctions()) {
      if (contains(text, f, offset)) {
        return callableScope(null, f, f.getParameters(), f.getBody(), text, offset);
      }
    }
    for (ClassDecl c : program.getClassDefs()) {
      if (!contains(text, c, offset)) {
        continue;
      }
      for (MethodDecl m : c.getMethods()) {
        if (contains(text, m, offset)) {
          return callableScope(c, m, m.getParameters(), m.getBody(), text, offset);
        }
      }
      for (ConstructorDecl k : c.getConstructors()) {
        if (!k.isSynthesized() && contains(text, k, offset)) {
          return callableScope(c, k, k.getParameters(), k.getBody(), text, offset);
        }
      }
      return new Scope(c, null, List.of());
    }
    return new Scope(null, null, List.of());
  }

  private static Scope callableScope(ClassDecl cls, Decl callable, List<ParameterDecl> parameters, BlockStmt body,
      SourceText text, int offset) {
    List<Decl> locals = new ArrayList<>(parameters);
    collectLocals(body.getStatements(), text, offset, locals);
    return new Scope(cls, callable, locals);
  }

  private static void collectLocals(List<Stmt> statements, SourceText text, int offset, List<Decl> out) {
    for (Stmt s : statements) {
      if (text.endOffset(s) <= offset) {
        if (s instanceof VariableStmt v) {
          out.add(v.getVariableDecl());
        }
      } else {
        if (text.startOffset(s) <= offset) {
          descend(s, text, offset, out);
        }
        return;
      }
    }
  }

  private static void descend(Stmt s, SourceText text, int offset, List<Decl> out) {
    switch (s) {
      case BlockStmt b -> collectLocals(b.getStatements(), text, offset, out);
      case IfStmt i -> {
        for (Stmt branch : new Stmt[] { i.getIfBranch(), i.getElseBranch() }) {
          if (branch != null && contains(text, branch, offset)) {
            descend(branch, text, offset, out);
          }
        }
      }
      case WhileStmt w -> {
        if (contains(text, w.getBody(), offset)) {
          descend(w.getBody(), text, offset, out);
        }
      }
      default -> {
      }
    }
  }
}
