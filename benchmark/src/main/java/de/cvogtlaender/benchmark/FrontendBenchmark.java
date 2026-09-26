package de.cvogtlaender.benchmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import de.cvogtlaender.interpreter.MiniCpp;

/**
 * Scalability of the static phases (lexing, parsing, AST construction,
 * resolving, type checking) with the size of the source: {@code classes}
 * classes, each with a few methods, plus as many functions using them.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class FrontendBenchmark {

  @Param({ "10", "100", "1000" })
  public int classes;

  private String source;

  @Setup(Level.Trial)
  public void generate() {
    source = Programs.largeProgram(classes);
    MiniCpp.Compilation c = MiniCpp.compile(source);
    if (c.hasErrors()) {
      throw new IllegalStateException("generated program does not compile: " + c.diagnostics());
    }
  }

  @Benchmark
  public Object parse() {
    return MiniCpp.parseProgram(source).tree();
  }

  @Benchmark
  public Object compile() {
    return MiniCpp.compile(source).program();
  }
}
