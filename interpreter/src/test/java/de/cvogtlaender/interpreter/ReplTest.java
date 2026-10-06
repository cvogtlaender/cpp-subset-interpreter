package de.cvogtlaender.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.cvogtlaender.interpreter.repl.Repl;
import de.cvogtlaender.interpreter.repl.Repl.Status;

class ReplTest {

  private ByteArrayOutputStream outBuffer;
  private ByteArrayOutputStream errBuffer;
  private Repl repl;

  @BeforeEach
  void setUp() {
    outBuffer = new ByteArrayOutputStream();
    errBuffer = new ByteArrayOutputStream();
    repl = new Repl(new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
        new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
  }

  private String out() {
    String s = outBuffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    outBuffer.reset();
    return s;
  }

  private String err() {
    String s = errBuffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    errBuffer.reset();
    return s;
  }

  @Test
  void evaluatesExpressionsAndKeepsVariables() {
    assertEquals(Status.OK, repl.eval("int x = 20;"));
    assertEquals(Status.OK, repl.eval("x = x + 1;"));
    assertEquals(Status.OK, repl.eval("x * 2"));
    assertEquals("42\n", out());
    assertEquals(Status.OK, repl.eval("\"s\\n\""));
    assertEquals(Status.OK, repl.eval("'c'"));
    assertEquals(Status.OK, repl.eval("x > 3"));
    assertEquals("\"s\\n\"\n'c'\ntrue\n", out());
  }

  @Test
  void statementsWithSemicolonPrintNothing() {
    assertEquals(Status.OK, repl.eval("1 + 2;"));
    assertEquals("", out());
    assertEquals(Status.OK, repl.eval("print_int(7)"));
    assertEquals("7\n", out());
  }

  @Test
  void definesFunctionsAndClasses() {
    assertEquals(Status.OK, repl.eval("int square(int n) { return n * n; }"));
    assertEquals(Status.OK, repl.eval("class P { public: int v; P(int x) { v = x; } int twice() { return 2 * v; } };"));
    assertEquals(Status.OK, repl.eval("P p(square(3));"));
    assertEquals(Status.OK, repl.eval("p.twice()"));
    assertEquals("18\n", out());
    assertEquals(Status.OK, repl.eval("p"));
    assertEquals("P{v = 9}\n", out());
  }

  @Test
  void multiLineInputIsIncompleteUntilClosed() {
    assertEquals(Status.INCOMPLETE, repl.eval("int f(int n) {\n"));
    assertEquals(Status.INCOMPLETE, repl.eval("int f(int n) {\n  if (n == 0) return 1;\n"));
    assertEquals(Status.OK, repl.eval("int f(int n) {\n  if (n == 0) return 1;\n  return n * f(n - 1);\n}\n"));
    assertEquals(Status.INCOMPLETE, repl.eval("f(5) +"));
    assertEquals(Status.OK, repl.eval("f(5)"));
    assertEquals("120\n", out());
  }

  @Test
  void isDefineBeforeUse() {
    assertEquals(Status.ERROR, repl.eval("int a() { return b(); }"));
    assertTrue(err().contains("use of undeclared identifier 'b'"));
    assertEquals(Status.ERROR, repl.eval("y = 1;"));
    assertTrue(err().contains("use of undeclared identifier 'y'"));
  }

  @Test
  void failedInputLeavesNoTrace() {
    assertEquals(Status.ERROR, repl.eval("int g() { return 1; } int z = true;"));
    assertTrue(err().contains("cannot initialize 'z'"));
    assertEquals(Status.ERROR, repl.eval("g()"));
    assertTrue(err().contains("use of undeclared identifier 'g'"));
    assertEquals(Status.OK, repl.eval("int g() { return 2; }"));
    assertEquals(Status.OK, repl.eval("g()"));
    assertEquals("2\n", out());
  }

  @Test
  void sessionVariablesAreNotVisibleInFunctions() {
    assertEquals(Status.OK, repl.eval("int counter = 0;"));
    assertEquals(Status.ERROR, repl.eval("void bump() { counter = counter + 1; }"));
    assertTrue(err().contains("session variable 'counter' cannot be used inside a function"));
  }

  @Test
  void variablesMayBeRedeclared() {
    assertEquals(Status.OK, repl.eval("int v = 1;"));
    assertEquals(Status.OK, repl.eval("string v = \"now a string\";"));
    assertEquals(Status.OK, repl.eval("v"));
    assertEquals("\"now a string\"\n", out());
  }

  @Test
  void runtimeErrorsAreReported() {
    assertEquals(Status.OK, repl.eval("int zero = 0;"));
    assertEquals(Status.ERROR, repl.eval("10 / zero"));
    assertTrue(err().contains("runtime error: division by zero"));
    assertEquals(Status.OK, repl.eval("zero + 1"));
    assertEquals("1\n", out());
  }

  @Test
  void returnIsNotAllowedAtThePrompt() {
    assertEquals(Status.ERROR, repl.eval("return 1;"));
    assertTrue(err().contains("'return' outside of a function"));
  }

  @Test
  void loadRunsMainInSessionScope() {
    String file = """
        int twice(int x) { return 2 * x; }
        int main() {
          int answer = twice(21);
          print_string("loaded");
          return 0;
        }
        """;
    assertEquals(Status.OK, repl.load(file));
    assertEquals("loaded\nmain() returned 0\n", out());
    assertEquals(Status.OK, repl.eval("answer + twice(1)"));
    assertEquals("44\n", out());
  }

  @Test
  void loadWithoutMainOnlyDeclares() {
    assertEquals(Status.OK, repl.load("int one() { return 1; }"));
    assertEquals(Status.OK, repl.eval("one()"));
    assertEquals("1\n", out());
  }

  @Test
  void interactiveLoopHandlesCommandsAndContinuations() throws IOException {
    String input = String.join("\n",
        "int x = 2;",
        "int f(int a) {",
        "  return a + x;",
        "}",
        "int f(int a) {",
        "  return a * 3;",
        "}",
        "f(x)",
        ":vars",
        ":functions",
        ":cancel",
        "int y = ",
        ":cancel",
        ":quit") + "\n";
    repl.run(new BufferedReader(new StringReader(input)));
    String output = out();
    assertTrue(output.contains("6\n"), output);
    assertTrue(output.contains("int x = 2"), output);
    assertTrue(output.contains("int f(int)"), output);
    assertTrue(output.contains("void print_int(int)   (built-in)"), output);
    assertTrue(err().contains("session variable 'x' cannot be used inside a function"));
  }
}
