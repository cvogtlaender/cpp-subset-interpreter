package de.cvogtlaender.mcp;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.cvogtlaender.interpreter.MiniCpp;
import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.cpp.CppExporter;
import de.cvogtlaender.interpreter.diagnostic.Diagnostic;

/**
 * The MiniC++ compiler and interpreter as tools for an LLM host (see
 * {@link McpStdioServer}). Unlike {@link AssistantService} no GenAI provider is
 * involved: the host's model does the reasoning and uses these tools to check
 * and run code.
 */
public class MiniCppTools {

  public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
  public static final Duration MAX_TIMEOUT = Duration.ofSeconds(60);
  static final int MAX_OUTPUT_BYTES = 100_000;

  /** A tool result: structured fields plus a text rendering for the model. */
  public interface Result {
    String text();
  }

  public record CheckResult(boolean ok, List<Diagnostic> diagnostics) implements Result {
    @Override
    public String text() {
      return ok ? "no errors" : formatAll(diagnostics);
    }
  }

  /** {@code exitCode} is null if the program did not compile or hit the time limit. */
  public record RunResult(boolean compiled, Integer exitCode, String output, boolean outputTruncated,
      boolean timedOut, List<Diagnostic> diagnostics) implements Result {
    @Override
    public String text() {
      if (!compiled) {
        return "compilation failed:\n" + formatAll(diagnostics);
      }
      StringBuilder sb = new StringBuilder("output:\n").append(output);
      if (!output.isEmpty() && !output.endsWith("\n")) {
        sb.append('\n');
      }
      if (outputTruncated) {
        sb.append("[output truncated after ").append(MAX_OUTPUT_BYTES).append(" bytes]\n");
      }
      if (!diagnostics.isEmpty()) {
        sb.append(formatAll(diagnostics)).append('\n');
      }
      sb.append(exitCode == null ? "program was stopped" : "exit code " + exitCode);
      return sb.toString();
    }
  }

  public record AstResult(String ast, List<Diagnostic> diagnostics) implements Result {
    @Override
    public String text() {
      return ast != null ? ast : formatAll(diagnostics);
    }
  }

  public record CppResult(String cpp, List<Diagnostic> diagnostics) implements Result {
    @Override
    public String text() {
      return cpp != null ? cpp : "compilation failed:\n" + formatAll(diagnostics);
    }
  }

  private final ExecutorService runner = Executors.newCachedThreadPool(task -> {
    Thread thread = new Thread(task, "minicpp-run");
    thread.setDaemon(true);
    return thread;
  });

  /** Compiler diagnostics; {@code main} is only required if the code defines one. */
  public CheckResult check(String code) {
    List<Diagnostic> diagnostics = AssistantService.check(code);
    return new CheckResult(diagnostics.isEmpty(), diagnostics);
  }

  /**
   * Compiles and runs a program, stopping it after {@code timeout}.
   *
   * @throws CancellationException if the calling thread is interrupted
   */
  public RunResult run(String code, Duration timeout) {
    MiniCpp.Compilation compilation = MiniCpp.compile(code);
    if (compilation.hasErrors()) {
      return new RunResult(false, null, "", false, false, compilation.diagnostics());
    }
    BoundedOutput output = new BoundedOutput(MAX_OUTPUT_BYTES);
    PrintStream out = new PrintStream(output, false, StandardCharsets.UTF_8);
    Future<MiniCpp.RunResult> execution = runner.submit(() -> MiniCpp.execute(compilation, out));
    try {
      MiniCpp.RunResult result = execution.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
      out.flush();
      return new RunResult(true, result.exitCode(), output.text(), output.truncated(), false,
          result.diagnostics());
    } catch (TimeoutException e) {
      // interrupts the interpreter thread, which stops at its next loop iteration or call
      execution.cancel(true);
      out.flush();
      Diagnostic stopped = new Diagnostic(Diagnostic.Phase.RUNTIME, 0, 0, 0, 0,
          "time limit of " + timeout.toSeconds() + " s exceeded (endless loop?)");
      return new RunResult(true, null, output.text(), output.truncated(), true, List.of(stopped));
    } catch (InterruptedException e) {
      execution.cancel(true);
      Thread.currentThread().interrupt();
      throw new CancellationException("run was cancelled");
    } catch (ExecutionException e) {
      if (e.getCause() instanceof RuntimeException re) {
        throw re;
      }
      throw new IllegalStateException(e.getCause());
    }
  }

  public AstResult ast(String code) {
    MiniCpp.ParseResult<Program> parsed = MiniCpp.parseProgram(code);
    return parsed.hasErrors()
        ? new AstResult(null, parsed.diagnostics())
        : new AstResult(parsed.tree().toStringTree(), List.of());
  }

  /** Translates a program to standard C++, e.g. for comparing with g++. */
  public CppResult toCpp(String code) {
    MiniCpp.Compilation compilation = MiniCpp.compile(code);
    return compilation.hasErrors()
        ? new CppResult(null, compilation.diagnostics())
        : new CppResult(CppExporter.export(compilation.program()), List.of());
  }

  private static String formatAll(List<Diagnostic> diagnostics) {
    StringBuilder sb = new StringBuilder();
    for (Diagnostic d : diagnostics) {
      sb.append(sb.isEmpty() ? "" : "\n").append(d.format());
    }
    return sb.toString();
  }

  /** Keeps the first {@code limit} bytes of the program output and drops the rest. */
  private static final class BoundedOutput extends OutputStream {
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final int limit;
    private boolean truncated;

    BoundedOutput(int limit) {
      this.limit = limit;
    }

    @Override
    public synchronized void write(int b) {
      write(new byte[] {(byte) b}, 0, 1);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
      int room = limit - bytes.size();
      if (len > room) {
        truncated = true;
      }
      bytes.write(b, off, Math.max(0, Math.min(len, room)));
    }

    synchronized String text() {
      return bytes.toString(StandardCharsets.UTF_8);
    }

    synchronized boolean truncated() {
      return truncated;
    }
  }
}
