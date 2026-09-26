package de.cvogtlaender.interpreter.cpp;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.ConstructorDecl;
import de.cvogtlaender.interpreter.ast.declaration.FieldDecl;
import de.cvogtlaender.interpreter.ast.declaration.FunctionDecl;
import de.cvogtlaender.interpreter.ast.declaration.MethodDecl;
import de.cvogtlaender.interpreter.ast.declaration.ParameterDecl;
import de.cvogtlaender.interpreter.ast.declaration.VariableDecl;
import de.cvogtlaender.interpreter.ast.expression.AssignExpr;
import de.cvogtlaender.interpreter.ast.expression.BinaryExpr;
import de.cvogtlaender.interpreter.ast.expression.BoolLiteral;
import de.cvogtlaender.interpreter.ast.expression.CallExpr;
import de.cvogtlaender.interpreter.ast.expression.CharLiteral;
import de.cvogtlaender.interpreter.ast.expression.Expr;
import de.cvogtlaender.interpreter.ast.expression.IntLiteral;
import de.cvogtlaender.interpreter.ast.expression.MemberAccessExpr;
import de.cvogtlaender.interpreter.ast.expression.NewExpr;
import de.cvogtlaender.interpreter.ast.expression.NullptrLiteral;
import de.cvogtlaender.interpreter.ast.expression.StringLiteral;
import de.cvogtlaender.interpreter.ast.expression.UnaryExpr;
import de.cvogtlaender.interpreter.ast.expression.VarExpr;
import de.cvogtlaender.interpreter.ast.statement.BlockStmt;
import de.cvogtlaender.interpreter.ast.statement.DeleteStmt;
import de.cvogtlaender.interpreter.ast.statement.ExprStmt;
import de.cvogtlaender.interpreter.ast.statement.IfStmt;
import de.cvogtlaender.interpreter.ast.statement.ReturnStmt;
import de.cvogtlaender.interpreter.ast.statement.Stmt;
import de.cvogtlaender.interpreter.ast.statement.VariableStmt;
import de.cvogtlaender.interpreter.ast.statement.WhileStmt;
import de.cvogtlaender.interpreter.ast.type.ClassType;
import de.cvogtlaender.interpreter.ast.type.PointerType;
import de.cvogtlaender.interpreter.ast.type.PrimitiveType;
import de.cvogtlaender.interpreter.ast.type.ReferenceType;
import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.semantic.Types;
import de.cvogtlaender.interpreter.visitor.TypeCheckVisitor;

/**
 * Translates a checked MiniC++ program into standard C++17 with the same
 * behavior, so that results can be compared with GCC. It bridges the places
 * where MiniC++ deliberately differs from C++:
 * <ul>
 * <li>define-after-use: classes are forward-declared and ordered by their
 * dependencies, functions get prototypes, and methods are defined out of line
 * after all classes;</li>
 * <li>variables and fields without initializer get MiniC++'s default value
 * (pointers: nullptr), as does 'new T' ('new T()' in C++);</li>
 * <li>string literals have type {@code string};</li>
 * <li>the print_* built-ins are defined in a prelude, and a {@code void
 * main()} or an {@code int main()} without return is wrapped;</li>
 * <li>base classes get a virtual destructor, so that deleting a derived
 * object through a base pointer is well-defined.</li>
 * </ul>
 */
public class CppExporter {

  private static final String MAIN = "minicpp_main";

  // C++ keywords and names that are ordinary identifiers in MiniC++
  private static final Set<String> RESERVED = Set.of("alignas", "alignof", "and", "and_eq", "asm", "auto",
      "bitand", "bitor", "break", "case", "catch", "char16_t", "char32_t", "char8_t", "compl", "concept", "const",
      "consteval", "constexpr", "constinit", "const_cast", "continue", "co_await", "co_return", "co_yield",
      "decltype", "default", "do", "double", "dynamic_cast", "enum", "explicit", "export", "extern",
      "float", "for", "friend", "goto", "inline", "long", "mutable", "namespace", "noexcept", "not",
      "not_eq", "operator", "or", "or_eq", "private", "protected", "register", "reinterpret_cast",
      "requires", "short", "signed", "sizeof", "static", "static_assert", "static_cast", "struct", "switch",
      "template", "this", "thread_local", "throw", "try", "typedef", "typeid", "typename", "union", "unsigned",
      "using", "volatile", "wchar_t", "xor", "xor_eq", "std", "minicpp_main");

  private static final String PRELUDE = """
      #include <iostream>
      #include <string>
      using std::string;

      static void print_bool(bool v) { std::cout << (v ? "true" : "false") << '\\n'; }
      static void print_int(int v) { std::cout << v << '\\n'; }
      static void print_char(char v) { std::cout << v << '\\n'; }
      static void print_string(string v) { std::cout << v << '\\n'; }
      """;

  private final StringBuilder out = new StringBuilder();
  private int indent;

  public static String export(Program program) {
    return new CppExporter().exportProgram(program);
  }

  private String exportProgram(Program program) {
    out.append("// Generated by 'minicpp to-cpp'. Compile with: g++ -std=c++17 -fwrapv\n");
    out.append(PRELUDE).append('\n');

    for (ClassDecl c : program.getClassDefs()) {
      out.append("class ").append(id(c.getClassName())).append(";\n");
    }
    if (!program.getClassDefs().isEmpty()) {
      out.append('\n');
    }

    for (FunctionDecl f : program.getFunctions()) {
      out.append(signature(f.getReturnType(), name(f), f.getParameters())).append(";\n");
    }
    out.append('\n');

    Set<String> baseClasses = program.getClassDefs().stream().map(ClassDecl::getParentClassName)
        .filter(p -> p != null).collect(Collectors.toSet());
    for (ClassDecl c : dependencyOrder(program.getClassDefs())) {
      classDefinition(c, c.getParentClassName() == null && baseClasses.contains(c.getClassName()));
    }

    for (ClassDecl c : program.getClassDefs()) {
      for (ConstructorDecl ctor : c.getConstructors()) {
        if (ctor.isSynthesized()) {
          continue;
        }
        out.append(id(c.getClassName())).append("::").append(id(c.getClassName())).append('(')
            .append(parameters(ctor.getParameters())).append(") ");
        block(ctor.getBody(), false);
        out.append("\n\n");
      }
      for (MethodDecl m : c.getMethods()) {
        out.append(signature(m.getReturnType(), id(c.getClassName()) + "::" + id(m.getName()), m.getParameters()))
            .append(' ');
        block(m.getBody(), false);
        out.append("\n\n");
      }
    }

    FunctionDecl main = null;
    for (FunctionDecl f : program.getFunctions()) {
      if (f.getName().equals("main")) {
        main = f;
      }
      out.append(signature(f.getReturnType(), name(f), f.getParameters())).append(' ');
      boolean implicitReturn = f.getName().equals("main") && !Types.isVoid(f.getReturnType())
          && !TypeCheckVisitor.alwaysReturns(f.getBody());
      block(f.getBody(), implicitReturn);
      out.append("\n\n");
    }

    if (main != null) {
      if (Types.isVoid(main.getReturnType())) {
        out.append("int main() {\n  ").append(MAIN).append("();\n  return 0;\n}\n");
      } else {
        out.append("int main() {\n  return ").append(MAIN).append("();\n}\n");
      }
    }
    return out.toString();
  }

  private static String name(FunctionDecl f) {
    return f.getName().equals("main") ? MAIN : id(f.getName());
  }

  private static String id(String name) {
    return RESERVED.contains(name) ? name + "_" : name;
  }

  private static String type(Type type) {
    return switch (type) {
      case ReferenceType r -> type(r.getReferencedType()) + "&";
      case PointerType p -> type(p.getPointeeType()) + "*";
      case ClassType c -> id(c.getName());
      default -> type.getName();
    };
  }

  /** Base classes and classes of by-value fields must be complete first. */
  private static List<ClassDecl> dependencyOrder(List<ClassDecl> classes) {
    Map<String, ClassDecl> byName = new HashMap<>();
    classes.forEach(c -> byName.put(c.getClassName(), c));
    Set<ClassDecl> ordered = new LinkedHashSet<>();
    for (ClassDecl c : classes) {
      addWithDependencies(c, byName, ordered);
    }
    return List.copyOf(ordered);
  }

  private static void addWithDependencies(ClassDecl c, Map<String, ClassDecl> byName, Set<ClassDecl> ordered) {
    if (c == null || ordered.contains(c)) {
      return;
    }
    if (c.getParentClassName() != null) {
      addWithDependencies(byName.get(c.getParentClassName()), byName, ordered);
    }
    for (FieldDecl f : c.getFields()) {
      if (f.getType() instanceof ClassType ct) {
        addWithDependencies(byName.get(ct.getName()), byName, ordered);
      }
    }
    ordered.add(c);
  }

  private void classDefinition(ClassDecl c, boolean virtualDestructor) {
    out.append("class ").append(id(c.getClassName()));
    if (c.getParentClassName() != null) {
      out.append(" : public ").append(id(c.getParentClassName()));
    }
    out.append(" {\npublic:\n");
    if (virtualDestructor) {
      out.append("  virtual ~").append(id(c.getClassName())).append("() = default;\n");
    }
    for (FieldDecl f : c.getFields()) {
      out.append("  ").append(type(f.getType())).append(' ').append(id(f.getName()))
          .append(defaultInitializer(f.getType())).append(";\n");
    }
    for (ConstructorDecl ctor : c.getConstructors()) {
      if (!ctor.isSynthesized()) {
        out.append("  ").append(id(c.getClassName())).append('(').append(parameters(ctor.getParameters()))
            .append(");\n");
      }
    }
    for (MethodDecl m : c.getMethods()) {
      out.append("  ").append(m.getIsVirtual() ? "virtual " : "")
          .append(signature(m.getReturnType(), id(m.getName()), m.getParameters())).append(";\n");
    }
    out.append("};\n\n");
  }

  private static String signature(Type returnType, String name, List<ParameterDecl> params) {
    return type(returnType) + " " + name + "(" + parameters(params) + ")";
  }

  private static String parameters(List<ParameterDecl> params) {
    return params.stream().map(p -> type(p.getType()) + " " + id(p.getName())).collect(Collectors.joining(", "));
  }

  /** MiniC++ initializes what C++ would leave indeterminate. */
  private static String defaultInitializer(Type type) {
    if (type instanceof PrimitiveType p) {
      return switch (p.getKind()) {
        case INT -> " = 0";
        case BOOL -> " = false";
        case CHAR -> " = '\\0'";
        default -> "";
      };
    }
    return type instanceof PointerType ? " = nullptr" : "";
  }

  // Statements

  private void block(BlockStmt block, boolean implicitReturnZero) {
    out.append("{\n");
    indent++;
    for (Stmt s : block.getStatements()) {
      statement(s);
    }
    if (implicitReturnZero) {
      line("return 0;");
    }
    indent--;
    pad();
    out.append('}');
  }

  private void statement(Stmt stmt) {
    switch (stmt) {
      case BlockStmt b -> {
        pad();
        block(b, false);
        out.append('\n');
      }
      case VariableStmt v -> {
        VariableDecl d = v.getVariableDecl();
        String init = d.getInitializer() != null ? " = " + expr(d.getInitializer()) : defaultInitializer(d.getType());
        line(type(d.getType()) + " " + id(d.getName()) + init + ";");
      }
      case ExprStmt e -> line(expr(e.getExpression()) + ";");
      case DeleteStmt d -> line("delete " + expr(d.getPointer()) + ";");
      case ReturnStmt r -> line(r.getReturnValue() == null ? "return;" : "return " + expr(r.getReturnValue()) + ";");
      case IfStmt i -> {
        line("if (" + expr(i.getCondition()) + ")");
        nested(i.getIfBranch());
        if (i.getElseBranch() != null) {
          line("else");
          nested(i.getElseBranch());
        }
      }
      case WhileStmt w -> {
        line("while (" + expr(w.getCondition()) + ")");
        nested(w.getBody());
      }
      default -> throw new IllegalStateException("unknown statement " + stmt);
    }
  }

  // branches are always emitted as blocks, which also avoids dangling-else issues
  private void nested(Stmt stmt) {
    if (stmt instanceof BlockStmt) {
      statement(stmt);
      return;
    }
    line("{");
    indent++;
    statement(stmt);
    indent--;
    line("}");
  }

  // Expressions (fully parenthesized, so precedence cannot change)

  private String expr(Expr e) {
    return switch (e) {
      case IntLiteral i -> i.getValue() == Integer.MIN_VALUE ? "(-2147483647 - 1)" : i.getValue().toString();
      case BoolLiteral b -> b.getValue().toString();
      case CharLiteral c -> "'" + escape(String.valueOf(c.getValue()), '\'') + "'";
      case StringLiteral s -> "string(\"" + escape(s.getValue(), '"') + "\")";
      case NullptrLiteral n -> "nullptr";
      case VarExpr v -> v.getName().equals("main") && v.getKind() == VarExpr.Kind.FUNCTION ? MAIN : id(v.getName());
      case MemberAccessExpr m -> expr(m.getObj()) + (m.isArrow() ? "->" : ".") + id(m.getMemberName());
      // '()' value-initializes, like MiniC++ does for 'new T'
      case NewExpr n -> "(new " + type(n.getAllocatedType()) + "("
          + n.getArguments().stream().map(this::expr).collect(Collectors.joining(", ")) + "))";
      case CallExpr c -> expr(c.getCallee()) + "("
          + c.getArguments().stream().map(this::expr).collect(Collectors.joining(", ")) + ")";
      case AssignExpr a -> "(" + expr(a.getTarget()) + " = " + expr(a.getValue()) + ")";
      case UnaryExpr u -> "(" + switch (u.getOperator()) {
        case NOT -> "!";
        case NEGATE -> "-";
        case POSITIVE -> "+";
        case DEREF -> "*";
        case ADDRESS_OF -> "&";
      } + expr(u.getExpr()) + ")";
      case BinaryExpr b -> "(" + expr(b.getLeftHandSide()) + " " + TypeCheckVisitor.symbol(b.getOperator()) + " "
          + expr(b.getRightHandSide()) + ")";
      default -> throw new IllegalStateException("cannot export " + e.toStringTree());
    };
  }

  private static String escape(String s, char quote) {
    StringBuilder sb = new StringBuilder();
    for (char c : s.toCharArray()) {
      switch (c) {
        case '\n' -> sb.append("\\n");
        case '\t' -> sb.append("\\t");
        case '\r' -> sb.append("\\r");
        case '\b' -> sb.append("\\b");
        // three digits, so that a following digit cannot extend the escape
        case '\0' -> sb.append("\\000");
        case '\\' -> sb.append("\\\\");
        default -> {
          if (c == quote) {
            sb.append('\\');
          }
          sb.append(c);
        }
      }
    }
    return sb.toString();
  }

  private void line(String text) {
    pad();
    out.append(text).append('\n');
  }

  private void pad() {
    out.append("  ".repeat(indent));
  }
}
