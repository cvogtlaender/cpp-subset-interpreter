package de.cvogtlaender.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.cvogtlaender.interpreter.MiniCpp;
import de.cvogtlaender.interpreter.diagnostic.Diagnostic;
import de.cvogtlaender.mcp.provider.GenAiException;
import de.cvogtlaender.mcp.provider.GenAiProvider;

public class AssistantService {

  static final String LANGUAGE = """
      MiniC++ is a small subset of C++. It has exactly these features:
      - types bool, int, char, string, void; references T& (variables must be initialized, parameters)
      - pointers T*, T** (&x, *p, p->m, nullptr, 'new T(args)', 'delete p;'); no pointer arithmetic
      - variables 'T x;' or 'T x = expr;', no global variables
      - operators + - * / % (int only), < <= > >= (int, char), == != (bool, int, char, string), && || !, =
      - if/else, while, blocks, return; no for, do, switch, break or continue
      - functions with overloading by exact parameter types; built-ins print_bool, print_int, print_char,
        print_string (each prints its argument and a newline)
      - classes 'class A { public: ... };' (everything public), constructors, fields, methods,
        single inheritance 'class D : public B', virtual methods (polymorphism through references and pointers)
      - entry point 'int main()' or 'void main()'
      It has NO arrays, casts, ++/--, compound assignments (+=), templates, static, const,
      'this', destructors, initializer lists, #include semantics or standard library.""";

  private static final Pattern CODE_BLOCK = Pattern.compile("```[a-zA-Z+]*\\s*\\n(.*?)```", Pattern.DOTALL);
  private static final Pattern FINDING = Pattern.compile("(?im)^\\s*[-*]?\\s*line\\s+(\\d+)\\s*[:\\-]\\s*(.+)$");

  private final GenAiProvider provider;

  public AssistantService(GenAiProvider provider) {
    this.provider = provider;
  }

  public GenAiProvider provider() {
    return provider;
  }

  public record Completion(String completion) {
  }

  public record Explanation(String explanation) {
  }

  public record Refactoring(String code, String rationale, boolean compiles, List<Diagnostic> diagnostics) {
  }

  public record Finding(int line, String description) {
  }

  public record BugReport(List<Diagnostic> compilerDiagnostics, List<Finding> findings, String analysis) {
  }

  public Completion complete(String code, int line, int column) throws GenAiException {
    int offset = offsetOf(code, line, column);
    String prompt = "Complete the MiniC++ code at the marker <CURSOR>.\n\n"
        + code.substring(0, offset) + "<CURSOR>" + code.substring(offset);
    String system = "You are a code completion engine for MiniC++.\n" + LANGUAGE + """

        Reply with ONLY the text to insert at <CURSOR>: no explanation, no markdown,
        and do not repeat code before or after the cursor.""";
    return new Completion(stripFences(provider.generate(system, prompt)));
  }

  public Explanation explain(String code) throws GenAiException {
    String system = "You explain MiniC++ code to students.\n" + LANGUAGE + """

        Explain what the code does, step by step and concisely. Mention the MiniC++ concepts
        involved (e.g. references, overloading, virtual dispatch, slicing) where relevant.""";
    return new Explanation(provider.generate(system, withDiagnostics(code, check(code))).strip());
  }

  public Refactoring refactor(String code, String instruction) throws GenAiException {
    String system = "Refactor MiniC++ code.\n" + LANGUAGE + """

        Improve readability and structure without changing behavior, using only MiniC++ features.
        Reply with the complete refactored code in a single ```cpp code block, followed by a line
        starting with 'Rationale:' that explains the changes.""";
    String prompt = (instruction == null || instruction.isBlank() ? "" : "Instruction: " + instruction + "\n\n")
        + code;
    String answer = provider.generate(system, prompt);

    Matcher block = CODE_BLOCK.matcher(answer);
    boolean fenced = block.find();
    String refactored = (fenced ? block.group(1) : answer).strip() + "\n";
    int rationaleAt = answer.indexOf("Rationale:");
    String rationale = rationaleAt >= 0
        ? answer.substring(rationaleAt + "Rationale:".length()).strip()
        : fenced ? answer.replace(block.group(), "").strip() : "";

    List<Diagnostic> diagnostics = check(refactored);
    return new Refactoring(refactored, rationale, diagnostics.isEmpty(), diagnostics);
  }

  public BugReport detectBugs(String code) throws GenAiException {
    List<Diagnostic> diagnostics = check(code);
    String system = "You review MiniC++ code for bugs.\n" + LANGUAGE + """

        Find potential runtime and logic errors (e.g. division by zero, infinite loops, unbounded
        recursion, off-by-one errors, unintended slicing, missing 'virtual', wrong use of references).
        Report each finding on its own line as 'LINE <number>: <description>'.
        If there are no problems, reply with 'NONE'.""";
    String analysis = provider.generate(system, withDiagnostics(numbered(code), diagnostics)).strip();

    List<Finding> findings = new ArrayList<>();
    Matcher m = FINDING.matcher(analysis);
    while (m.find()) {
      findings.add(new Finding(Integer.parseInt(m.group(1)), m.group(2).strip()));
    }
    return new BugReport(diagnostics, findings, analysis);
  }

  static List<Diagnostic> check(String code) {
    MiniCpp.ParseResult<de.cvogtlaender.interpreter.ast.Program> parsed = MiniCpp.parseProgram(code);
    if (parsed.hasErrors()) {
      return parsed.diagnostics();
    }
    boolean hasMain = parsed.tree().getFunctions().stream().anyMatch(f -> f.getName().equals("main"));
    return MiniCpp.analyze(parsed.tree(), hasMain).diagnostics();
  }

  private static String withDiagnostics(String code, List<Diagnostic> diagnostics) {
    if (diagnostics.isEmpty()) {
      return code;
    }
    StringBuilder sb = new StringBuilder(code).append("\n\nThe MiniC++ compiler reports:\n");
    for (Diagnostic d : diagnostics) {
      sb.append("- ").append(d.format()).append('\n');
    }
    return sb.toString();
  }

  private static String numbered(String code) {
    String[] lines = code.split("\n", -1);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < lines.length; i++) {
      sb.append(String.format("%4d| %s%n", i + 1, lines[i].stripTrailing()));
    }
    return sb.toString();
  }

  static String stripFences(String text) {
    Matcher block = CODE_BLOCK.matcher(text);
    String result = block.find() ? block.group(1) : text;
    return result.stripTrailing();
  }

  static int offsetOf(String code, int line, int column) {
    if (line < 1 || column < 0) {
      throw new IllegalArgumentException("line must be >= 1 and column >= 0");
    }
    int offset = 0;
    for (int l = 1; l < line; l++) {
      int newline = code.indexOf('\n', offset);
      if (newline < 0) {
        throw new IllegalArgumentException("line " + line + " is beyond the end of the code");
      }
      offset = newline + 1;
    }
    int lineEnd = code.indexOf('\n', offset);
    int lineLength = (lineEnd < 0 ? code.length() : lineEnd) - offset;
    if (column > lineLength) {
      throw new IllegalArgumentException("column " + column + " is beyond the end of line " + line);
    }
    return offset + column;
  }
}
