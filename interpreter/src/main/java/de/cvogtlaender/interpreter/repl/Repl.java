package de.cvogtlaender.interpreter.repl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.cvogtlaender.interpreter.MiniCpp;
import de.cvogtlaender.interpreter.ast.AstNode;
import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.ast.ReplInput;
import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.Decl;
import de.cvogtlaender.interpreter.ast.declaration.FunctionDecl;
import de.cvogtlaender.interpreter.ast.declaration.VariableDecl;
import de.cvogtlaender.interpreter.ast.expression.Expr;
import de.cvogtlaender.interpreter.ast.statement.Stmt;
import de.cvogtlaender.interpreter.ast.statement.VariableStmt;
import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.diagnostic.Diagnostic;
import de.cvogtlaender.interpreter.runtime.Interpreter;
import de.cvogtlaender.interpreter.runtime.MiniCppRuntimeException;
import de.cvogtlaender.interpreter.runtime.Values;
import de.cvogtlaender.interpreter.semantic.GlobalScope;
import de.cvogtlaender.interpreter.semantic.Types;
import de.cvogtlaender.interpreter.visitor.ASTResolveVisitor;
import de.cvogtlaender.interpreter.visitor.TypeCheckVisitor;

/**
 * Interactive read-eval-print loop. Each input may contain class and function
 * definitions, statements, and a trailing expression without ';' whose value
 * is printed. Variables live in the session scope, functions and classes in
 * the global scope; define-before-use applies.
 *
 * An input is checked as a whole before anything runs; if it has errors, none
 * of its declarations are kept.
 */
public class Repl {

  public enum Status {
    OK, INCOMPLETE, ERROR
  }

  private static final String PROMPT = "minicpp> ";
  private static final String CONTINUATION_PROMPT = "     ...> ";

  private final PrintStream out;
  private final PrintStream err;

  private GlobalScope globals;
  private ASTResolveVisitor resolver;
  private TypeCheckVisitor checker;
  private Interpreter interpreter;

  public Repl(PrintStream out, PrintStream err) {
    this.out = out;
    this.err = err;
    reset();
  }

  public final void reset() {
    globals = new GlobalScope();
    resolver = new ASTResolveVisitor(globals);
    checker = new TypeCheckVisitor(globals);
    interpreter = new Interpreter(globals, out);
  }

  /**
   * Loads a source file into the session: its classes and functions become
   * global, and if it defines {@code main()}, main runs in the session scope so
   * that its variables remain accessible.
   */
  public Status load(String source) {
    MiniCpp.ParseResult<Program> parsed = MiniCpp.parseProgram(source);
    if (parsed.hasErrors()) {
      report(parsed.diagnostics());
      return Status.ERROR;
    }
    Program program = parsed.tree();

    GlobalScope.Snapshot snapshot = globals.snapshot();
    resolver.getDiagnostics().clear();
    checker.getDiagnostics().clear();

    resolver.resolve(program);
    if (!resolver.hasErrors()) {
      checker.check(program);
    }
    List<Diagnostic> errors = resolver.hasErrors() ? resolver.getDiagnostics() : checker.getDiagnostics();
    if (!errors.isEmpty()) {
      globals.restore(snapshot);
      report(errors);
      return Status.ERROR;
    }

    FunctionDecl main = program.getFunctions().stream()
        .filter(f -> f.getName().equals("main") && f.getParameters().isEmpty())
        .findFirst().orElse(null);
    if (main == null) {
      return Status.OK;
    }

    try {
      Object result = MiniCpp.onLargeStack(() -> interpreter.runMainInSession(main));
      if (result != null) {
        out.println("main() returned " + Values.display(result));
      }
    } catch (MiniCppRuntimeException e) {
      report(List.of(e.toDiagnostic()));
      return Status.ERROR;
    } finally {
      // main's top-level variables stay visible in the session
      for (Stmt stmt : main.getBody().getStatements()) {
        if (stmt instanceof VariableStmt v) {
          VariableDecl decl = v.getVariableDecl();
          if (interpreter.isSessionVariableDefined(decl)) {
            globals.getSession().put(decl.getName(), decl);
          }
        }
      }
    }
    return Status.OK;
  }

  /** Evaluates one complete input. Returns INCOMPLETE if more lines are needed. */
  public Status eval(String input) {
    MiniCpp.ParseResult<ReplInput> parsed = MiniCpp.parseReplInput(input);
    if (parsed.hasErrors()) {
      if (parsed.incomplete()) {
        return Status.INCOMPLETE;
      }
      report(parsed.diagnostics());
      return Status.ERROR;
    }
    ReplInput chunk = parsed.tree();

    // check everything first
    GlobalScope.Snapshot snapshot = globals.snapshot();
    resolver.getDiagnostics().clear();
    checker.getDiagnostics().clear();
    Type trailingType = null;

    for (AstNode item : chunk.items()) {
      switch (item) {
        case ClassDecl c -> {
          resolver.declareClass(c);
          if (!resolver.hasErrors()) {
            checker.checkClass(c);
          }
        }
        case FunctionDecl f -> {
          resolver.declareFunction(f);
          if (!resolver.hasErrors()) {
            checker.checkFunction(f);
          }
        }
        case Stmt s -> {
          resolver.resolveSessionStatement(s);
          if (!resolver.hasErrors()) {
            checker.checkSessionStatement(s);
          }
        }
        default -> throw new IllegalStateException("unexpected REPL item " + item);
      }
      if (resolver.hasErrors() || checker.hasErrors()) {
        break;
      }
    }
    Expr trailing = chunk.trailingExpr();
    if (trailing != null && !resolver.hasErrors() && !checker.hasErrors()) {
      resolver.resolveSessionExpr(trailing);
      if (!resolver.hasErrors()) {
        trailingType = checker.checkSessionExpr(trailing);
      }
    }

    if (resolver.hasErrors() || checker.hasErrors()) {
      globals.restore(snapshot);
      report(resolver.hasErrors() ? resolver.getDiagnostics() : checker.getDiagnostics());
      return Status.ERROR;
    }

    // then run it
    try {
      Type shownType = trailingType;
      MiniCpp.onLargeStack(() -> {
        for (AstNode item : chunk.items()) {
          if (item instanceof Stmt s) {
            interpreter.executeSessionStatement(s);
          }
        }
        if (trailing != null) {
          Object value = interpreter.evaluateSessionExpr(trailing);
          if (shownType != null && !Types.isVoid(shownType)) {
            out.println(Values.display(value));
          }
        }
        return null;
      });
    } catch (MiniCppRuntimeException e) {
      report(List.of(e.toDiagnostic()));
      return Status.ERROR;
    }
    return Status.OK;
  }

  /** Runs the interactive loop until end of input or ':quit'. */
  public void run(BufferedReader in) throws IOException {
    out.println("MiniC++ REPL - enter declarations, statements or expressions. Type :help for help.");
    StringBuilder buffer = new StringBuilder();
    while (true) {
      out.print(buffer.isEmpty() ? PROMPT : CONTINUATION_PROMPT);
      out.flush();
      String line = in.readLine();
      if (line == null) {
        out.println();
        return;
      }

      if (buffer.isEmpty() && line.trim().startsWith(":")) {
        if (!command(line.trim())) {
          return;
        }
        continue;
      }
      if (line.trim().equals(":cancel")) {
        buffer.setLength(0);
        continue;
      }

      buffer.append(line).append('\n');
      if (buffer.toString().isBlank()) {
        buffer.setLength(0);
        continue;
      }
      if (eval(buffer.toString()) != Status.INCOMPLETE) {
        buffer.setLength(0);
      }
    }
  }

  /** Handles a ':' command. Returns false to quit. */
  private boolean command(String line) {
    String[] parts = line.split("\\s+", 2);
    switch (parts[0]) {
      case ":q", ":quit", ":exit" -> {
        return false;
      }
      case ":help", ":h" -> out.println("""
          Enter MiniC++ code:
            int x = 3;                      declare a session variable
            int sq(int n) { return n*n; }   define a function
            class A { public: int v; };     define a class
            sq(x) + 1                       evaluate an expression (no ';') and print it
          Incomplete input continues on the next line; ':cancel' discards it.
          Commands:
            :vars          list session variables
            :functions     list functions
            :classes       list classes
            :load <file>   load a file (runs its main() in the session)
            :reset         forget everything
            :quit          leave the REPL""");
      case ":vars" -> {
        for (Map.Entry<String, Decl> e : globals.getSession().entrySet()) {
          VariableDecl decl = (VariableDecl) e.getValue();
          Object value = interpreter.sessionValue(decl);
          out.println(decl.getType().getName() + " " + e.getKey() + " = "
              + (value == null ? "<uninitialized>" : Values.display(value)));
        }
      }
      case ":functions" -> globals.getFunctions().values().stream().flatMap(List::stream)
          .map(f -> f.getReturnType().getName() + " " + Types.signature(f.getName(), f.getParameters())
              + (f.isBuiltin() ? "   (built-in)" : ""))
          .forEach(out::println);
      case ":classes" -> globals.getClasses().values().forEach(c -> out.println("class " + c.getClassName()
          + (c.getParentClassName() == null ? "" : " : public " + c.getParentClassName())
          + " { " + c.getFields().stream().map(f -> f.getType().getName() + " " + f.getName() + ";")
              .collect(Collectors.joining(" "))
          + " }"));
      case ":load" -> {
        if (parts.length < 2) {
          err.println("usage: :load <file>");
        } else {
          try {
            load(Files.readString(Path.of(parts[1].trim())));
          } catch (IOException e) {
            err.println("cannot read " + parts[1].trim() + ": " + e.getMessage());
          }
        }
      }
      case ":reset" -> {
        reset();
        out.println("session reset");
      }
      default -> err.println("unknown command " + parts[0] + " (try :help)");
    }
    return true;
  }

  private void report(List<Diagnostic> diagnostics) {
    for (Diagnostic d : diagnostics) {
      err.println(d.format());
    }
    err.flush();
  }
}
