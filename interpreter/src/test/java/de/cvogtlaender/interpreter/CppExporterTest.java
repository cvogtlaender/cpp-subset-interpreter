package de.cvogtlaender.interpreter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import de.cvogtlaender.interpreter.cpp.CppExporter;

/**
 * The C++ export is compiled with g++ by scripts/compare-gcc.sh (in CI); here
 * we only check its structure.
 */
class CppExporterTest {

  private static String export(String source) {
    MiniCpp.Compilation compilation = MiniCpp.compile(source);
    assertFalse(compilation.hasErrors(), () -> compilation.diagnostics().toString());
    return CppExporter.export(compilation.program());
  }

  static java.util.stream.Stream<Path> programs() throws IOException, URISyntaxException {
    return ProgramTest.programs();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("programs")
  void exportsAllTestPrograms(Path program) throws IOException {
    String cpp = export(Files.readString(program));
    assertTrue(cpp.contains("#include <iostream>"));
    assertTrue(cpp.contains("int main() {"));
  }

  @Test
  void resolvesDefineAfterUse() {
    String cpp = export("""
        class A : public B { public: int get() { return helper(b) + later(); } };
        class B { public: int b; };
        int later() { return 1; }
        int helper(int x) { return x; }
        int main() { A a; return a.get(); }
        """);
    // prototypes, then classes with bases first, then out-of-line methods
    assertTrue(cpp.indexOf("int later();") < cpp.indexOf("class B {"));
    assertTrue(cpp.indexOf("class B {") < cpp.indexOf("class A : public B {"));
    assertTrue(cpp.indexOf("class A : public B {") < cpp.indexOf("int A::get() {"));
    assertTrue(cpp.contains("  int b = 0;"));
  }

  @Test
  void bridgesSemanticDifferences() {
    String cpp = export("""
        void main() {
          int x;
          char c;
          string s = "a\\n\\0" ;
          bool eq = "x" == s;
          int min = -2147483648;
        }
        """);
    assertTrue(cpp.contains("int x = 0;"));
    assertTrue(cpp.contains("char c = '\\0';"));
    assertTrue(cpp.contains("string s = string(\"a\\n\\000\");"));
    assertTrue(cpp.contains("(string(\"x\") == s)"));
    assertTrue(cpp.contains("(-2147483647 - 1)"));
    assertTrue(cpp.contains("void minicpp_main() {"));
    assertTrue(cpp.contains("int main() {\n  minicpp_main();\n  return 0;\n}"));
  }

  @Test
  void intMainWithoutReturnReturnsZero() {
    String cpp = export("int main() { print_int(1); }");
    assertTrue(cpp.contains("print_int(1);\n  return 0;\n}"));
  }

  @Test
  void renamesCppKeywords() {
    String cpp = export("""
        class this { public: int friend; };
        int sizeof(int auto) { return auto; }
        int main() { this t; t.friend = sizeof(2); return t.friend; }
        """);
    assertTrue(cpp.contains("class this_ {"));
    assertTrue(cpp.contains("int friend_ = 0;"));
    assertTrue(cpp.contains("int sizeof_(int auto_)"));
    assertTrue(cpp.contains("(t.friend_ = sizeof_(2));"));
  }

  @Test
  void exportsPointers() {
    String cpp = export("""
        class B { public: int v; };
        class D : public B { public: };
        int main() {
          B* b = new D;
          b->v = 1;
          int* p = &b->v;
          int* q;
          *p = *p + 1;
          delete b;
          return 0;
        }
        """);
    assertTrue(cpp.contains("  virtual ~B() = default;"), cpp);
    assertFalse(cpp.contains("~D()"), cpp);
    assertTrue(cpp.contains("B* b = (new D());"), cpp);
    assertTrue(cpp.contains("(b->v = 1);"), cpp);
    assertTrue(cpp.contains("int* p = (&b->v);"), cpp);
    assertTrue(cpp.contains("int* q = nullptr;"), cpp);
    assertTrue(cpp.contains("((*p) = ((*p) + 1));"), cpp);
    assertTrue(cpp.contains("delete b;"), cpp);
  }
}
