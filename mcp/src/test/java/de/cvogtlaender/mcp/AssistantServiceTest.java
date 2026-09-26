package de.cvogtlaender.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.cvogtlaender.mcp.provider.GenAiException;
import de.cvogtlaender.mcp.provider.MockProvider;

class AssistantServiceTest {

  private MockProvider provider;
  private AssistantService assistant;

  @BeforeEach
  void setUp() {
    provider = new MockProvider();
    assistant = new AssistantService(provider);
  }

  @Test
  void completionMarksTheCursorAndStripsFences() throws GenAiException {
    provider.respondWith((system, prompt) -> "```cpp\n  return n * fact(n - 1);\n```");
    String code = "int fact(int n) {\n  if (n == 0) return 1;\n\n}";
    AssistantService.Completion c = assistant.complete(code, 3, 0);
    assertEquals("  return n * fact(n - 1);", c.completion());
    String prompt = provider.requests().get(0).prompt();
    assertTrue(prompt.contains("return 1;\n<CURSOR>\n}"), prompt);
  }

  @Test
  void cursorOutsideTheCodeIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> assistant.complete("int x;", 2, 0));
    assertThrows(IllegalArgumentException.class, () -> assistant.complete("int x;", 1, 7));
    assertThrows(IllegalArgumentException.class, () -> assistant.complete("int x;", 0, 0));
  }

  @Test
  void offsets() {
    assertEquals(0, AssistantService.offsetOf("ab\ncd", 1, 0));
    assertEquals(2, AssistantService.offsetOf("ab\ncd", 1, 2));
    assertEquals(4, AssistantService.offsetOf("ab\ncd", 2, 1));
  }

  @Test
  void explanationIncludesCompilerErrorsInThePrompt() throws GenAiException {
    assistant.explain("int main() { return x; }");
    String prompt = provider.requests().get(0).prompt();
    assertTrue(prompt.contains("use of undeclared identifier 'x'"), prompt);
    assertTrue(provider.requests().get(0).system().contains("no for, do, switch"));
  }

  @Test
  void refactoringIsValidatedByTheCompiler() throws GenAiException {
    provider.respondWith((system, prompt) -> """
        Here you go:
        ```cpp
        int square(int n) {
          return n * n;
        }
        ```
        Rationale: extracted a function.""");
    AssistantService.Refactoring r = assistant.refactor("int square(int n) { int r = n * n; return r; }", null);
    assertTrue(r.compiles(), () -> r.diagnostics().toString());
    assertEquals("int square(int n) {\n  return n * n;\n}\n", r.code());
    assertEquals("extracted a function.", r.rationale());
  }

  @Test
  void invalidRefactoringIsFlagged() throws GenAiException {
    // the model used a for loop, which MiniC++ does not have
    provider.respondWith((system, prompt) -> """
        ```cpp
        int sum(int n) { int s = 0; for (int i = 0; i < n; i++) s += i; return s; }
        ```
        Rationale: shorter.""");
    AssistantService.Refactoring r = assistant.refactor("int sum(int n) { return 0; }", "use a loop");
    assertFalse(r.compiles());
    assertFalse(r.diagnostics().isEmpty());
    assertTrue(provider.requests().get(0).prompt().startsWith("Instruction: use a loop"));
  }

  @Test
  void bugFindingsAreParsed() throws GenAiException {
    provider.respondWith((system, prompt) -> """
        LINE 3: division by zero when d is 0
        - line 5 - loop never terminates""");
    String code = "int f(int d) {\n  int x = 1;\n  return x / d;\n}\nint main() { while (true) { } }";
    AssistantService.BugReport report = assistant.detectBugs(code);
    assertTrue(report.compilerDiagnostics().isEmpty(), () -> report.compilerDiagnostics().toString());
    assertEquals(2, report.findings().size());
    assertEquals(3, report.findings().get(0).line());
    assertEquals("division by zero when d is 0", report.findings().get(0).description());
    assertEquals(5, report.findings().get(1).line());
    assertTrue(provider.requests().get(0).prompt().contains("   3|   return x / d;"));
  }

  @Test
  void bugReportContainsCompilerDiagnostics() throws GenAiException {
    AssistantService.BugReport report = assistant.detectBugs("int main() { int x = true; }");
    assertEquals(1, report.compilerDiagnostics().size());
    assertTrue(report.findings().isEmpty());
    assertEquals("NONE", report.analysis());
  }

  @Test
  void codeWithoutMainIsNotAnError() {
    assertTrue(AssistantService.check("int f() { return 1; }").isEmpty());
    assertFalse(AssistantService.check("int main() { return f(); }").isEmpty());
  }
}
