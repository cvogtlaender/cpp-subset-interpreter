package de.cvogtlaender.lsp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Position;

import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.Decl;
import de.cvogtlaender.interpreter.ast.declaration.FieldDecl;
import de.cvogtlaender.interpreter.ast.declaration.FunctionDecl;
import de.cvogtlaender.interpreter.ast.declaration.MethodDecl;
import de.cvogtlaender.interpreter.ast.type.ClassType;
import de.cvogtlaender.interpreter.ast.type.PointerType;
import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.semantic.GlobalScope;
import de.cvogtlaender.interpreter.semantic.Types;

/**
 * Completion of keywords, types, classes, functions, variables in scope and,
 * after {@code .} and {@code ->}, members of the object's class.
 *
 * While the user types, the text usually does not parse ({@code a.} is
 * incomplete), so the semantic information comes from the last analysis that
 * had an AST, and the object before {@code .}/{@code ->} is resolved from the
 * text: a chain like {@code a.b->c().} is followed through the declarations.
 */
public final class Completions {

  static final List<String> TYPES = List.of("int", "bool", "char", "string", "void");
  static final List<String> STATEMENT_KEYWORDS = List.of("if", "else", "while", "return", "new", "delete",
      "nullptr", "true", "false");
  static final List<String> DECLARATION_KEYWORDS = List.of("class", "public", "virtual");

  private Completions() {
  }

  private record Segment(String name, boolean call) {
  }

  public static List<CompletionItem> complete(Analysis current, Position position) {
    String src = current.text().text();
    int offset = current.text().offset(position);
    if (inCommentOrLiteral(src, offset)) {
      return List.of();
    }

    int prefixStart = offset;
    while (prefixStart > 0 && Character.isJavaIdentifierPart(src.charAt(prefixStart - 1))) {
      prefixStart--;
    }
    int before = skipSpaceBack(src, prefixStart);
    boolean dot = before > 0 && src.charAt(before - 1) == '.';
    boolean arrow = before > 1 && src.charAt(before - 1) == '>' && src.charAt(before - 2) == '-';

    Analysis semantic = current.lastParsed();
    if (dot || arrow) {
      if (semantic == null) {
        return List.of();
      }
      AstNodes.Scope scope = AstNodes.scopeAt(semantic.program(), semantic.text(),
          semantic.text().offset(position));
      ClassDecl cls = receiverClass(src, before - (dot ? 1 : 2), scope, semantic.globals());
      return cls == null ? List.of() : members(cls);
    }
    if (before > 0 && src.charAt(before - 1) == '>' && prefixStart == offset) {
      return List.of(); // triggered by '>' of a comparison
    }

    List<CompletionItem> items = new ArrayList<>();
    AstNodes.Scope scope = semantic == null ? null
        : AstNodes.scopeAt(semantic.program(), semantic.text(), semantic.text().offset(position));
    boolean inBody = scope == null || scope.callable() != null;

    if (scope != null) {
      Set<String> seen = new HashSet<>();
      for (int i = scope.locals().size() - 1; i >= 0; i--) {
        Decl local = scope.locals().get(i);
        if (seen.add(Names.of(local))) {
          items.add(item(Names.of(local), CompletionItemKind.Variable, Names.detail(local), "0"));
        }
      }
      if (scope.enclosingClass() != null && scope.callable() != null) {
        items.addAll(members(scope.enclosingClass()));
      }
      GlobalScope globals = semantic.globals();
      if (inBody) {
        for (List<FunctionDecl> overloads : globals.getFunctions().values()) {
          for (FunctionDecl f : overloads) {
            items.add(item(f.getName(), CompletionItemKind.Function, Names.detail(f), "2"));
          }
        }
      }
      for (ClassDecl c : globals.getClasses().values()) {
        items.add(item(c.getClassName(), CompletionItemKind.Class, Names.detail(c), "2"));
      }
    }

    for (String type : TYPES) {
      items.add(item(type, CompletionItemKind.Keyword, null, "3"));
    }
    if (inBody) {
      for (String keyword : STATEMENT_KEYWORDS) {
        items.add(item(keyword, CompletionItemKind.Keyword, null, "3"));
      }
    }
    if (scope == null || scope.callable() == null) {
      for (String keyword : DECLARATION_KEYWORDS) {
        items.add(item(keyword, CompletionItemKind.Keyword, null, "3"));
      }
    }
    return items;
  }

  /** Fields and methods of a class including inherited ones, without hidden base members. */
  static List<CompletionItem> members(ClassDecl cls) {
    List<CompletionItem> items = new ArrayList<>();
    Set<String> hidden = new HashSet<>();
    for (ClassDecl c = cls; c != null; c = c.getParent()) {
      Set<String> declaredHere = new HashSet<>();
      for (FieldDecl f : c.getFields()) {
        if (!hidden.contains(f.getName())) {
          items.add(item(f.getName(), CompletionItemKind.Field, Names.detail(f), "1"));
        }
        declaredHere.add(f.getName());
      }
      for (MethodDecl m : c.getMethods()) {
        if (!hidden.contains(m.getName())) {
          items.add(item(m.getName(), CompletionItemKind.Method, Names.detail(m), "1"));
        }
        declaredHere.add(m.getName());
      }
      hidden.addAll(declaredHere);
    }
    return items;
  }

  /**
   * The class of the object whose member access operator starts at
   * {@code operator}, resolved from a chain of names, calls and member
   * accesses in the text. Null if it cannot be determined.
   */
  private static ClassDecl receiverClass(String src, int operator, AstNodes.Scope scope, GlobalScope globals) {
    List<Segment> chain = new ArrayList<>();
    int i = operator;
    while (true) {
      i = skipSpaceBack(src, i);
      boolean call = false;
      if (i > 0 && src.charAt(i - 1) == ')') {
        i = skipSpaceBack(src, openingParen(src, i - 1));
        if (i < 0) {
          return null;
        }
        call = true;
      }
      int end = i;
      while (i > 0 && Character.isJavaIdentifierPart(src.charAt(i - 1))) {
        i--;
      }
      if (i == end) {
        return null;
      }
      chain.add(0, new Segment(src.substring(i, end), call));
      int j = skipSpaceBack(src, i);
      if (j > 0 && src.charAt(j - 1) == '.') {
        i = j - 1;
      } else if (j > 1 && src.charAt(j - 1) == '>' && src.charAt(j - 2) == '-') {
        i = j - 2;
      } else {
        break;
      }
    }

    Type type = first(chain.get(0), scope, globals);
    for (Segment segment : chain.subList(1, chain.size())) {
      ClassDecl cls = classOf(type, globals);
      if (cls == null) {
        return null;
      }
      type = member(cls, segment);
    }
    return classOf(type, globals);
  }

  private static Type first(Segment segment, AstNodes.Scope scope, GlobalScope globals) {
    String name = segment.name();
    if (!segment.call()) {
      Decl local = scope.local(name);
      if (local != null) {
        return Names.typeOf(local);
      }
      return scope.enclosingClass() == null ? null : member(scope.enclosingClass(), segment);
    }
    if (scope.enclosingClass() != null) {
      Type method = member(scope.enclosingClass(), segment);
      if (method != null) {
        return method;
      }
    }
    List<FunctionDecl> functions = globals.getFunctions().get(name);
    if (functions != null && !functions.isEmpty()) {
      return functions.get(0).getReturnType();
    }
    ClassDecl cls = globals.getClass(name);
    return cls == null ? null : new ClassType(cls.getClassName());
  }

  // type of a field access or return type of a method call on cls
  private static Type member(ClassDecl cls, Segment segment) {
    ClassDecl owner = GlobalScope.findMemberOwner(cls, segment.name());
    if (owner == null) {
      return null;
    }
    if (!segment.call()) {
      FieldDecl field = GlobalScope.findField(owner, segment.name());
      return field == null ? null : field.getType();
    }
    List<MethodDecl> methods = GlobalScope.methodsNamed(owner, segment.name());
    return methods.isEmpty() ? null : methods.get(0).getReturnType();
  }

  // the class of a value, reference or pointer
  private static ClassDecl classOf(Type type, GlobalScope globals) {
    if (type == null) {
      return null;
    }
    Type stripped = Types.strip(type);
    if (stripped instanceof PointerType p) {
      stripped = p.getPointeeType();
    }
    return globals.classOf(stripped);
  }

  private static int openingParen(String src, int close) {
    int depth = 0;
    for (int i = close; i >= 0; i--) {
      char c = src.charAt(i);
      if (c == ')') {
        depth++;
      } else if (c == '(' && --depth == 0) {
        return i;
      }
    }
    return -1;
  }

  private static int skipSpaceBack(String src, int i) {
    while (i > 0 && Character.isWhitespace(src.charAt(i - 1))) {
      i--;
    }
    return i;
  }

  // a cheap check on the current line: after '//' or inside a string or char literal
  private static boolean inCommentOrLiteral(String src, int offset) {
    int lineStart = src.lastIndexOf('\n', offset - 1) + 1;
    char quote = 0;
    for (int i = lineStart; i < offset; i++) {
      char c = src.charAt(i);
      if (quote != 0) {
        if (c == '\\') {
          i++;
        } else if (c == quote) {
          quote = 0;
        }
      } else if (c == '"' || c == '\'') {
        quote = c;
      } else if (c == '/' && i + 1 < offset && src.charAt(i + 1) == '/') {
        return true;
      }
    }
    return quote != 0;
  }

  private static CompletionItem item(String label, CompletionItemKind kind, String detail, String group) {
    CompletionItem item = new CompletionItem(label);
    item.setKind(kind);
    item.setDetail(detail);
    item.setSortText(group + label);
    return item;
  }
}
