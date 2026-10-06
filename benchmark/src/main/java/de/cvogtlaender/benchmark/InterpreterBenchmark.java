package de.cvogtlaender.benchmark;

import java.io.OutputStream;
import java.io.PrintStream;
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

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class InterpreterBenchmark {

  private static final PrintStream NULL_OUT = new PrintStream(OutputStream.nullOutputStream());

  @Param({ "15", "20", "25" })
  public int n;

  private MiniCpp.Compilation fib;
  private MiniCpp.Compilation loop;
  private MiniCpp.Compilation dispatch;
  private MiniCpp.Compilation objects;

  @Setup(Level.Trial)
  public void compile() {
    fib = compileOrFail(Programs.fib(n));
    loop = compileOrFail(Programs.loop(n * 10_000));
    dispatch = compileOrFail(Programs.virtualDispatch(n * 1_000));
    objects = compileOrFail(Programs.objectCopies(n * 1_000));
  }

  static MiniCpp.Compilation compileOrFail(String source) {
    MiniCpp.Compilation c = MiniCpp.compile(source);
    if (c.hasErrors()) {
      throw new IllegalStateException("benchmark program does not compile: " + c.diagnostics());
    }
    return c;
  }

  @Benchmark
  public int recursiveFib() {
    return MiniCpp.execute(fib, NULL_OUT).exitCode();
  }

  @Benchmark
  public int arithmeticLoop() {
    return MiniCpp.execute(loop, NULL_OUT).exitCode();
  }

  @Benchmark
  public int virtualDispatch() {
    return MiniCpp.execute(dispatch, NULL_OUT).exitCode();
  }

  @Benchmark
  public int objectCopies() {
    return MiniCpp.execute(objects, NULL_OUT).exitCode();
  }
}
