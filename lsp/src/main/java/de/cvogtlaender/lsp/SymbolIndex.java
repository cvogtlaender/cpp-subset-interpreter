package de.cvogtlaender.lsp;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.antlr.v4.runtime.Token;

import de.cvogtlaender.interpreter.MiniCppLexer;
import de.cvogtlaender.interpreter.ast.AstNode;
import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.ConstructorDecl;
import de.cvogtlaender.interpreter.ast.declaration.Decl;
import de.cvogtlaender.interpreter.ast.declaration.FieldDecl;
import de.cvogtlaender.interpreter.ast.declaration.FunctionDecl;
import de.cvogtlaender.interpreter.ast.declaration.MethodDecl;
import de.cvogtlaender.interpreter.ast.declaration.ParameterDecl;
import de.cvogtlaender.interpreter.ast.declaration.VariableDecl;
import de.cvogtlaender.interpreter.ast.expression.CallExpr;
import de.cvogtlaender.interpreter.ast.expression.Expr;
import de.cvogtlaender.interpreter.ast.expression.MemberAccessExpr;
import de.cvogtlaender.interpreter.ast.expression.NewExpr;
import de.cvogtlaender.interpreter.ast.expression.VarExpr;
import de.cvogtlaender.interpreter.semantic.GlobalScope;

/**
 * Every identifier in a program that names a declaration, found by combining
 * the resolved AST with the token stream (AST nodes know their range but not
 * where exactly their name is).
 *
 * Each occurrence has a {@code target}, the exact declaration it refers to
 * (used for go-to-definition and hover), and a {@code symbol} grouping all
 * occurrences that must be renamed together: constructors belong to their
 * class, overriding methods to the base method they override.
 */
public final class SymbolIndex {

  public record Occurrence(int start, int end, Decl symbol, Decl target, boolean declaration) {
  }

  private final TreeMap<Integer, Occurrence> occurrences = new TreeMap<>();
  // declaration -> occurrence of its name in its declaration
  private final Map<Decl, Occurrence> declarations = new IdentityHashMap<>();
  private final Tokens tokens;
  private final SourceText text;
  private final GlobalScope globals;

  private SymbolIndex(Tokens tokens, SourceText text, GlobalScope globals) {
    this.tokens = tokens;
    this.text = text;
    this.globals = globals;
  }

  public static SymbolIndex build(Program program, GlobalScope globals, Tokens tokens, SourceText text) {
    SymbolIndex index = new SymbolIndex(tokens, text, globals);
    index.visit(program);
    return index;
  }

  /** The occurrence under the cursor, including a cursor right after the identifier. */
  public Occurrence at(int offset) {
    var entry = occurrences.floorEntry(offset);
    if (entry != null && offset <= entry.getValue().end()) {
      return entry.getValue();
    }
    return null;
  }

  /** All occurrences of a symbol in source order. */
  public List<Occurrence> occurrencesOf(Decl symbol) {
    List<Occurrence> result = new ArrayList<>();
    for (Occurrence o : occurrences.values()) {
      if (o.symbol() == symbol) {
        result.add(o);
      }
    }
    return result;
  }

  /** The name in the declaration of {@code decl}, or null (built-ins, implicit constructors). */
  public Occurrence declarationOf(Decl decl) {
    return declarations.get(decl);
  }

  /** The symbol that occurrences of {@code decl} are grouped under. */
  public static Decl canonical(Decl decl) {
    if (decl instanceof ConstructorDecl k && k.getOwner() != null) {
      return k.getOwner();
    }
    if (decl instanceof MethodDecl m) {
      for (MethodDecl base = Names.overridden(m); base != null; base = Names.overridden(base)) {
        m = base;
      }
      return m;
    }
    return decl;
  }

  // Traversal

  private void visit(AstNode node) {
    switch (node) {
      case ClassDecl c -> {
        int i = tokenAt(c);
        declare(i + 1, c, c);
        if (c.getParent() != null && type(i + 2) == MiniCppLexer.COLON) {
          reference(i + 4, c.getParent(), c.getParent());
        }
      }
      case ConstructorDecl k -> {
        Occurrence o = add(tokenAt(k), k.getOwner() != null ? k.getOwner() : k, k, false);
        if (o != null) {
          declarations.put(k, o);
        }
      }
      case FunctionDecl f -> declare(skipType(tokenAt(f)), f, f);
      case MethodDecl m -> {
        int i = tokenAt(m);
        declare(skipType(type(i) == MiniCppLexer.VIRTUAL ? i + 1 : i), canonical(m), m);
      }
      case FieldDecl f -> declare(skipType(tokenAt(f)), f, f);
      case ParameterDecl p -> declare(skipType(tokenAt(p)), p, p);
      case VariableDecl v -> declare(skipType(tokenAt(v)), v, v);
      case NewExpr n -> {
        int i = tokenAt(n) + 1;
        ClassDecl cls = globals.getClass(text(i));
        if (cls != null) {
          reference(i, cls, n.getConstructor() != null ? n.getConstructor() : cls);
        }
      }
      case CallExpr call -> {
        if (call.getCallee() instanceof VarExpr v) {
          variable(v, call);
        } else if (call.getCallee() instanceof MemberAccessExpr m) {
          visit(m.getObj());
          member(m, call);
        } else {
          visit(call.getCallee());
        }
        for (Expr arg : call.getArguments()) {
          visit(arg);
        }
        return;
      }
      case VarExpr v -> variable(v, null);
      case MemberAccessExpr m -> {
        visit(m.getObj());
        member(m, null);
        return;
      }
      default -> {
      }
    }
    for (AstNode child : AstNodes.children(node)) {
      visit(child);
    }
  }

  private void variable(VarExpr v, CallExpr call) {
    Decl callTarget = call == null ? null : call.getTarget();
    if (v.getKind() == null) {
      return;
    }
    Decl target = switch (v.getKind()) {
      case VARIABLE, FIELD -> v.getResolvedDecl();
      case CLASS -> callTarget != null ? callTarget : v.getResolvedDecl();
      case FUNCTION -> {
        if (callTarget != null) {
          yield callTarget;
        }
        List<FunctionDecl> overloads = globals.getFunctions().get(v.getName());
        yield overloads == null || overloads.isEmpty() ? null : overloads.get(0);
      }
      case METHOD -> {
        if (callTarget != null || v.getMemberOwner() == null) {
          yield callTarget;
        }
        List<MethodDecl> overloads = GlobalScope.methodsNamed(v.getMemberOwner(), v.getName());
        yield overloads.isEmpty() ? null : overloads.get(0);
      }
    };
    if (target != null) {
      reference(tokenAt(v), canonical(target), target);
    }
  }

  private void member(MemberAccessExpr m, CallExpr call) {
    Decl target = m.getResolvedField() != null ? m.getResolvedField() : call == null ? null : call.getTarget();
    if (target == null) {
      return;
    }
    // the member name is the last token of the expression
    int i = tokens.indexStartingAt(text.endOffset(m) - m.getMemberName().length());
    if (m.getMemberName().equals(text(i))) {
      reference(i, canonical(target), target);
    }
  }

  // Helpers

  private void declare(int tokenIndex, Decl symbol, Decl decl) {
    Occurrence o = add(tokenIndex, symbol, decl, true);
    if (o != null) {
      declarations.put(decl, o);
    }
  }

  private void reference(int tokenIndex, Decl symbol, Decl target) {
    add(tokenIndex, symbol, target, false);
  }

  private Occurrence add(int tokenIndex, Decl symbol, Decl target, boolean declaration) {
    Token token = tokens.get(tokenIndex);
    if (!Tokens.isIdentifier(token) || symbol == null) {
      return null;
    }
    Occurrence o = new Occurrence(Tokens.start(token), Tokens.end(token), symbol, target, declaration);
    occurrences.put(o.start(), o);
    return o;
  }

  /**
   * Skips a type starting at token {@code i} ({@code T}, {@code T*},
   * {@code T*&}, ...), recording a class name as a reference. Returns the
   * index of the token after the type.
   */
  private int skipType(int i) {
    if (type(i) == MiniCppLexer.Identifier) {
      ClassDecl cls = globals.getClass(text(i));
      if (cls != null) {
        reference(i, cls, cls);
      }
    }
    i++;
    while (type(i) == MiniCppLexer.STAR) {
      i++;
    }
    if (type(i) == MiniCppLexer.AMP) {
      i++;
    }
    return i;
  }

  private int tokenAt(AstNode node) {
    return tokens.indexStartingAt(text.startOffset(node));
  }

  private int type(int i) {
    Token t = tokens.get(i);
    return t == null ? Token.EOF : t.getType();
  }

  private String text(int i) {
    Token t = tokens.get(i);
    return t == null ? "" : t.getText();
  }
}
