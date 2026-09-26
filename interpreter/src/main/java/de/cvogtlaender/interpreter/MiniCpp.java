package de.cvogtlaender.interpreter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.ast.ReplInput;
import de.cvogtlaender.interpreter.diagnostic.Diagnostic;
import de.cvogtlaender.interpreter.runtime.Interpreter;
import de.cvogtlaender.interpreter.runtime.MiniCppRuntimeException;
import de.cvogtlaender.interpreter.semantic.GlobalScope;
import de.cvogtlaender.interpreter.visitor.ASTBuildVisitor;
import de.cvogtlaender.interpreter.visitor.ASTResolveVisitor;
import de.cvogtlaender.interpreter.visitor.TypeCheckVisitor;

/**
 * Entry point into the MiniC++ pipeline:
 * source -> lexer -> parser -> AST -> resolver -> type checker -> interpreter.
 */
public final class MiniCpp {

  /** Stack size for the interpreter thread; tree walking needs deep stacks. */
  private static final long INTERPRETER_STACK_SIZE = 1L << 29;

  private MiniCpp() {
  }

  public record ParseResult<T>(T tree, List<Diagnostic> diagnostics, boolean incomplete) {
    public boolean hasErrors() {
      return !diagnostics.isEmpty();
    }
  }

  /** Result of the static phases. {@code globals} is null if parsing failed. */
  public record Compilation(Program program, GlobalScope globals, List<Diagnostic> diagnostics) {
    public boolean hasErrors() {
      return !diagnostics.isEmpty();
    }
  }

  public record RunResult(int exitCode, List<Diagnostic> diagnostics) {
    public boolean hasErrors() {
      return !diagnostics.isEmpty();
    }
  }

  // Parsing

  public static ParseResult<Program> parseProgram(String source) {
    return parse(source, MiniCppParser::program, tree -> (Program) tree.accept(new ASTBuildVisitor()));
  }

  public static ParseResult<ReplInput> parseReplInput(String source) {
    return parse(source, MiniCppParser::replInput, tree -> (ReplInput) tree.accept(new ASTBuildVisitor()));
  }

  private static <T> ParseResult<T> parse(String source, Function<MiniCppParser, ParseTree> rule,
      Function<ParseTree, T> build) {
    SyntaxErrorCollector errors = new SyntaxErrorCollector();

    MiniCppLexer lexer = new MiniCppLexer(CharStreams.fromString(source));
    lexer.removeErrorListeners();
    lexer.addErrorListener(errors);

    MiniCppParser parser = new MiniCppParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(errors);

    ParseTree tree = rule.apply(parser);
    if (!errors.diagnostics.isEmpty()) {
      return new ParseResult<>(null, errors.diagnostics, errors.reachedEof);
    }
    return new ParseResult<>(build.apply(tree), errors.diagnostics, false);
  }

  private static final class SyntaxErrorCollector extends BaseErrorListener {
    final List<Diagnostic> diagnostics = new ArrayList<>();
    boolean reachedEof;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int column, String msg,
        RecognitionException e) {
      int length = 1;
      if (offendingSymbol instanceof Token token) {
        if (token.getType() == Token.EOF) {
          reachedEof = true;
        } else {
          length = Math.max(1, token.getText().length());
        }
      }
      diagnostics.add(new Diagnostic(Diagnostic.Phase.SYNTAX, line, column, line, column + length, msg));
    }
  }

  // Static analysis

  public static Compilation compile(String source) {
    ParseResult<Program> parsed = parseProgram(source);
    if (parsed.hasErrors()) {
      return new Compilation(null, null, parsed.diagnostics());
    }
    return analyze(parsed.tree(), true);
  }

  /** Resolves and type-checks a parsed program. */
  public static Compilation analyze(Program program, boolean requireMain) {
    GlobalScope globals = new GlobalScope();
    ASTResolveVisitor resolver = new ASTResolveVisitor(globals);
    resolver.resolve(program);
    if (resolver.hasErrors()) {
      return new Compilation(program, globals, resolver.getDiagnostics());
    }

    TypeCheckVisitor checker = new TypeCheckVisitor(globals);
    checker.check(program);
    if (requireMain) {
      checker.checkEntryPoint(program);
    }
    return new Compilation(program, globals, checker.getDiagnostics());
  }

  // Execution

  /** Compiles and runs a program, writing its output to {@code out}. */
  public static RunResult run(String source, PrintStream out) {
    Compilation compilation = compile(source);
    if (compilation.hasErrors()) {
      return new RunResult(1, compilation.diagnostics());
    }
    return execute(compilation, out);
  }

  public static RunResult execute(Compilation compilation, PrintStream out) {
    Interpreter interpreter = new Interpreter(compilation.globals(), out);
    try {
      int exitCode = onLargeStack(() -> interpreter.run(compilation.program()));
      return new RunResult(exitCode, List.of());
    } catch (MiniCppRuntimeException e) {
      out.flush();
      return new RunResult(1, List.of(e.toDiagnostic()));
    }
  }

  /** Runs {@code task} on a thread with a large stack, rethrowing its exceptions. */
  public static <T> T onLargeStack(Callable<T> task) {
    AtomicReference<T> result = new AtomicReference<>();
    AtomicReference<Throwable> failure = new AtomicReference<>();
    Thread thread = new Thread(null, () -> {
      try {
        result.set(task.call());
      } catch (Throwable t) {
        failure.set(t);
      }
    }, "minicpp-interpreter", INTERPRETER_STACK_SIZE);
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      thread.interrupt();
      Thread.currentThread().interrupt();
      throw new MiniCppRuntimeException("interrupted", null);
    }
    Throwable t = failure.get();
    if (t instanceof RuntimeException re) {
      throw re;
    }
    if (t instanceof Error err) {
      throw err;
    }
    if (t != null) {
      throw new IllegalStateException(t);
    }
    return result.get();
  }
}
