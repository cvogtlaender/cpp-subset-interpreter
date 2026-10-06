package de.cvogtlaender.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.cvogtlaender.interpreter.diagnostic.Diagnostic;
import de.cvogtlaender.interpreter.diagnostic.Diagnostic.Phase;

class ErrorTest {

    static Stream<Arguments> invalidPrograms() {
        return Stream.of(
                err(Phase.SYNTAX, "", "int main() { int x = ; }"),
                err(Phase.SYNTAX, "", "int main() { int x = 0; x++; }"),
                err(Phase.SYNTAX, "", "int main() { int* p; p[0] = 1; }"),
                err(Phase.SYNTAX, "", "int main() { int x = 1; x += 1; }"),
                err(Phase.SYNTAX, "", "int main() { int& r; }"),

                err(Phase.RESOLVE, "use of undeclared identifier 'break'", "int main() { while (true) { break; } }"),
                err(Phase.SYNTAX, "", "int x; int main() { }"),
                err(Phase.SYNTAX, "", "int f(); int main() { }"),
                err(Phase.SYNTAX, "", "class A { int x; }; int main() { }"),

                err(Phase.RESOLVE, "use of undeclared identifier 'y'", "int main() { int x = y; }"),
                err(Phase.RESOLVE, "use of undeclared identifier 'x'", "int main() { x = 1; int x; }"),
                err(Phase.RESOLVE, "redeclaration of 'x'", "int main() { int x; bool x; }"),
                err(Phase.RESOLVE, "redeclaration of 'a'", "void f(int a) { int a = 1; } int main() { }"),
                err(Phase.RESOLVE, "unknown type 'Foo'", "int main() { Foo f; }"),
                err(Phase.RESOLVE, "unknown base class 'B'", "class A : public B { public: }; int main() { }"),
                err(Phase.RESOLVE, "inherits from itself",
                        "class A : public B { public: }; class B : public A { public: }; int main() { }"),
                err(Phase.RESOLVE, "redefinition of class 'A'",
                        "class A { public: }; class A { public: }; int main() { }"),
                err(Phase.RESOLVE, "redefinition of function 'f(int)'",
                        "void f(int a) { } void f(int b) { } int main() { }"),
                err(Phase.RESOLVE, "redefinition of function 'f(int)'",
                        "void f(int a) { } int f(int b) { return 1; } int main() { }"),
                err(Phase.RESOLVE, "redefinition of built-in function 'print_int(int)'",
                        "void print_int(int x) { } int main() { }"),
                err(Phase.RESOLVE, "function 'A' conflicts with a class",
                        "int A() { return 1; } class A { public: }; int main() { }"),
                err(Phase.RESOLVE, "duplicate member 'x'", "class A { public: int x; bool x; }; int main() { }"),
                err(Phase.RESOLVE, "already declared in a base class",
                        "class A { public: int x; }; class B : public A { public: int x; }; int main() { }"),
                err(Phase.RESOLVE, "conflicts with a field",
                        "class A { public: int x; int x() { return 1; } }; int main() { }"),
                err(Phase.RESOLVE, "must be named like its class", "class A { public: B() { } }; int main() { }"),
                err(Phase.RESOLVE, "redefinition of constructor",
                        "class A { public: A(int x) { } A(int y) { } }; int main() { }"),
                err(Phase.RESOLVE, "redefinition of method",
                        "class A { public: void m() { } int m() { return 1; } }; int main() { }"),
                err(Phase.RESOLVE, "'void' is only allowed as a return type", "int main() { void v; }"),
                err(Phase.RESOLVE, "'void' is only allowed as a return type", "void f(void x) { } int main() { }"),
                err(Phase.RESOLVE, "use of undeclared identifier 'x'",
                        "int main() { { int x = 1; } print_int(x); }"),
                err(Phase.RESOLVE, "pointers to 'void' are not supported", "int main() { void* p; }"),
                err(Phase.RESOLVE, "unknown type 'Foo'", "int main() { Foo** p; }"),
                err(Phase.RESOLVE, "unknown type 'Foo'", "int main() { new Foo; }"),
                err(Phase.RESOLVE, "cannot allocate an object of type 'void'", "int main() { new void; }"),

                err(Phase.TYPE, "cannot initialize 'x' of type 'int' with a value of type 'bool'",
                        "int main() { int x = true; }"),
                err(Phase.TYPE, "invalid operands to binary '+' ('int' and 'bool')",
                        "int main() { int x = 1 + true; }"),
                err(Phase.TYPE, "invalid operands to binary '+' ('string' and 'string')",
                        "int main() { string s = \"a\" + \"b\"; }"),
                err(Phase.TYPE, "invalid operands to binary '+' ('char' and 'int')",
                        "int main() { char c = 'a' + 1; }"),
                err(Phase.TYPE, "invalid operands to binary '<' ('string' and 'string')",
                        "int main() { bool b = \"a\" < \"b\"; }"),
                err(Phase.TYPE, "invalid operands to binary '<' ('bool' and 'bool')",
                        "int main() { bool b = true < false; }"),
                err(Phase.TYPE, "invalid operands to binary '==' ('int' and 'char')",
                        "int main() { bool b = 1 == 'a'; }"),
                err(Phase.TYPE, "invalid operands to binary '&&' ('int' and 'bool')",
                        "int main() { bool b = 1 && true; }"),
                err(Phase.TYPE, "invalid operand to unary '!' ('int')", "int main() { bool b = !1; }"),
                err(Phase.TYPE, "invalid operand to unary '-' ('bool')", "int main() { int x = -true; }"),
                err(Phase.TYPE, "'if' condition of type 'string' cannot be converted to bool",
                        "int main() { if (\"s\") { } }"),
                err(Phase.TYPE, "'while' condition of type 'A'",
                        "class A { public: }; int main() { A a; while (a) { } }"),
                err(Phase.TYPE, "expression is not assignable", "int main() { 1 = 2; }"),
                err(Phase.TYPE, "expression is not assignable", "int f() { return 1; } int main() { f() = 2; }"),
                err(Phase.TYPE, "cannot assign a value of type 'char' to 'int'", "int main() { int x; x = 'c'; }"),
                err(Phase.TYPE, "invalid operands to binary '==' ('A' and 'A')",
                        "class A { public: }; int main() { A a; A b; bool e = a == b; }"),
                err(Phase.TYPE, "integer literal '99999999999' is out of range", "int main() { return 99999999999; }"),
                err(Phase.TYPE, "only supported for class types", "int main() { int x(5); }"),

                err(Phase.TYPE, "no matching function for call to 'f(int, int)'; candidates are: f(int)",
                        "int f(int x) { return x; } int main() { f(1, 2); }"),
                err(Phase.TYPE, "no matching function for call to 'print_int(string)'",
                        "int main() { print_int(\"1\"); }"),
                err(Phase.TYPE, "call to 'f(int)' is ambiguous",
                        "void f(int x) { } void f(int& x) { } int main() { int a = 1; f(a); }"),
                err(Phase.TYPE, "no matching function for call to 'f(int)'", "void f(int& x) { } int main() { f(1); }"),
                err(Phase.TYPE, "call to 'f(D)' is ambiguous",
                        "class B { public: }; class C : public B { public: }; class D : public C { public: };"
                                + " void f(B b) { } void f(C c) { } int main() { D d; f(d); }"),
                err(Phase.TYPE, "'x' is not a function", "int main() { int x; x(); }"),
                err(Phase.TYPE, "'main' is a function; it must be called", "int main() { main; }"),
                err(Phase.TYPE, "'A' is a class name, not a value", "class A { public: }; int main() { A; }"),
                err(Phase.TYPE, "no matching function for call to 'A(int)'",
                        "class A { public: }; int main() { A a = A(1); }"),
                err(Phase.TYPE, "no matching function for call to 'print_int(void)'",
                        "void g() { } int main() { print_int(g()); }"),
                err(Phase.TYPE, "cannot initialize 'x' of type 'int' with a value of type 'void'",
                        "void g() { } int main() { int x = g(); }"),

                err(Phase.TYPE, "class 'A' has no member named 'y'",
                        "class A { public: int x; }; int main() { A a; a.y = 1; }"),
                err(Phase.TYPE, "method 'm' must be called",
                        "class A { public: void m() { } }; int main() { A a; a.m; }"),
                err(Phase.TYPE, "'x' is a field of 'A', not a method",
                        "class A { public: int x; }; int main() { A a; a.x(); }"),
                err(Phase.TYPE, "member access '.y' on a value of non-class type 'int'", "int main() { int x; x.y; }"),
                err(Phase.TYPE, "class 'B' has no member named 'onlyDerived'",
                        "class B { public: }; class D : public B { public: int onlyDerived; };"
                                + " int main() { D d; B& b = d; b.onlyDerived = 1; }"),

                err(Phase.TYPE, "cannot bind reference 'r' to a temporary value", "int main() { int& r = 5; }"),
                err(Phase.TYPE, "cannot bind 'int&' to a value of type 'char'", "int main() { char c; int& r = c; }"),
                err(Phase.TYPE, "cannot bind 'D&' to a value of type 'B'",
                        "class B { public: }; class D : public B { public: }; int main() { B b; D& d = b; }"),
                err(Phase.TYPE, "cannot initialize 'd' of type 'D' with a value of type 'B'",
                        "class B { public: }; class D : public B { public: }; int main() { B b; D d = b; }"),
                err(Phase.TYPE, "class 'A' has no parameterless constructor",
                        "class A { public: A(int x) { } }; int main() { A a; }"),

                err(Phase.TYPE, "invalid operand to unary '*' ('int')", "int main() { int x; int y = *x; }"),
                err(Phase.TYPE, "cannot take the address of a temporary value", "int main() { int* p = &5; }"),
                err(Phase.TYPE, "cannot take the address of a temporary value",
                        "int f() { return 1; } int main() { int* p = &f(); }"),
                err(Phase.TYPE, "cannot initialize 'p' of type 'int*' with a value of type 'int'",
                        "int main() { int x; int* p = x; }"),
                err(Phase.TYPE, "cannot initialize 'p' of type 'int*' with a value of type 'char*'",
                        "int main() { char c; int* p = &c; }"),
                err(Phase.TYPE, "cannot initialize 'x' of type 'int' with a value of type 'nullptr_t'",
                        "int main() { int x = nullptr; }"),
                err(Phase.TYPE, "cannot initialize 'd' of type 'D*' with a value of type 'B*'",
                        "class B { public: }; class D : public B { public: }; int main() { B b; D* d = &b; }"),
                err(Phase.TYPE, "cannot initialize 'pp' of type 'B**' with a value of type 'D**'",
                        "class B { public: }; class D : public B { public: }; int main() { D* d; B** pp = &d; }"),
                err(Phase.TYPE, "invalid operands to binary '==' ('int*' and 'char*')",
                        "int main() { int* p; char* q; bool b = p == q; }"),
                err(Phase.TYPE, "invalid operands to binary '<' ('int*' and 'int*')",
                        "int main() { int* p; int* q; bool b = p < q; }"),
                err(Phase.TYPE, "invalid operands to binary '+' ('int*' and 'int')",
                        "int main() { int* p; p = p + 1; }"),
                err(Phase.TYPE, "invalid operand to unary '!' ('int*')", "int main() { int* p; bool b = !p; }"),
                err(Phase.TYPE, "member access '.v' on a value of non-class type 'A*'; did you mean to use '->'?",
                        "class A { public: int v; }; int main() { A* a; a.v = 1; }"),
                err(Phase.TYPE, "member access '->v' on a value of type 'A', which is not a pointer to a class",
                        "class A { public: int v; }; int main() { A a; a->v = 1; }"),
                err(Phase.TYPE, "member access '->m' on a value of type 'int*', which is not a pointer to a class",
                        "int main() { int* p; p->m(); }"),
                err(Phase.TYPE, "cannot delete a value of non-pointer type 'int'", "int main() { int x; delete x; }"),
                err(Phase.TYPE, "cannot delete a value of non-pointer type 'nullptr_t'",
                        "int main() { delete nullptr; }"),
                err(Phase.TYPE, "call to 'f(nullptr_t)' is ambiguous",
                        "void f(int* p) { } void f(char* p) { } int main() { f(nullptr); }"),
                err(Phase.TYPE, "no matching function for call to 'f(D*)'",
                        "class B { public: }; class D : public B { public: };"
                                + " void f(B*& p) { } int main() { D* d; f(d); }"),
                err(Phase.TYPE, "too many initializers for 'new int'", "int main() { int* p = new int(1, 2); }"),
                err(Phase.TYPE, "cannot initialize a new 'int' with a value of type 'bool'",
                        "int main() { int* p = new int(true); }"),
                err(Phase.TYPE, "no matching function for call to 'A(int)'",
                        "class A { public: }; int main() { A* a = new A(1); }"),
                err(Phase.TYPE, "cannot return a value of type 'int*' from 'f' returning 'int'",
                        "int f(int* p) { return p; } int main() { }"),

                err(Phase.TYPE, "non-void function 'f' does not return a value on all paths",
                        "int f(int x) { if (x > 0) { return 1; } } int main() { }"),
                err(Phase.TYPE, "non-void function 'f' does not return a value on all paths",
                        "int f() { while (1 == 1) { return 1; } } int main() { }"),
                err(Phase.TYPE, "void function 'f' cannot return a value", "void f() { return 1; } int main() { }"),
                err(Phase.TYPE, "non-void function 'f' must return a value of type 'int'",
                        "int f() { return; } int main() { }"),
                err(Phase.TYPE, "cannot return a value of type 'string' from 'f' returning 'int'",
                        "int f() { return \"x\"; } int main() { }"),
                err(Phase.TYPE, "constructor 'A' cannot return a value",
                        "class A { public: A() { return 1; } }; int main() { }"),
                err(Phase.TYPE, "non-void function 'A::get' does not return a value on all paths",
                        "class A { public: int get() { } }; int main() { }"),

                err(Phase.TYPE, "base class 'A' of 'B' has no parameterless constructor",
                        "class A { public: A(int x) { } }; class B : public A { public: }; int main() { }"),
                err(Phase.TYPE, "field 'a' of type 'A' requires a parameterless constructor",
                        "class A { public: A(int x) { } }; class B { public: A a; }; int main() { }"),
                err(Phase.TYPE, "class 'A' contains itself", "class A { public: A inner; }; int main() { }"),
                err(Phase.TYPE, "contains itself",
                        "class A { public: B b; }; class B { public: A a; }; int main() { }"),
                err(Phase.TYPE, "differs from the overridden method in 'B'",
                        "class B { public: virtual int f() { return 1; } };"
                                + " class D : public B { public: string f() { return \"\"; } }; int main() { }"),

                err(Phase.TYPE, "no 'main' function defined", "int foo() { return 0; }"),
                err(Phase.TYPE, "'main' must not take parameters", "int main(int argc) { return 0; }"),
                err(Phase.TYPE, "'main' must return 'int' or 'void'", "string main() { return \"\"; }"));
    }

    private static Arguments err(Phase phase, String message, String source) {
        return Arguments.of(phase, message, source);
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("invalidPrograms")
    void isRejected(Phase phase, String message, String source) {
        MiniCpp.Compilation compilation = MiniCpp.compile(source);
        assertTrue(compilation.hasErrors(), "expected an error for: " + source);
        Diagnostic first = compilation.diagnostics().get(0);
        assertEquals(phase, first.phase(), () -> "diagnostics: " + compilation.diagnostics());
        assertTrue(first.message().contains(message),
                () -> "expected '" + message + "' but got: " + compilation.diagnostics());
    }

    @Test
    void errorsCarrySourcePositions() {
        MiniCpp.Compilation compilation = MiniCpp.compile("int main() {\n  int x = 1;\n  x = y;\n}");
        Diagnostic d = compilation.diagnostics().get(0);
        assertEquals(3, d.line());
        assertEquals(6, d.column());
        assertEquals("3:7: error: use of undeclared identifier 'y'", d.format());
    }

    @Test
    void reportsAllResolverErrors() {
        MiniCpp.Compilation compilation = MiniCpp.compile("int main() { a = 1; b = 2; }");
        assertEquals(2, compilation.diagnostics().size());
    }

    @Test
    void syntaxErrorsDoNotCrash() {
        MiniCpp.Compilation compilation = MiniCpp.compile("class { int main( { ");
        assertFalse(compilation.diagnostics().isEmpty());
        assertEquals(Phase.SYNTAX, compilation.diagnostics().get(0).phase());
    }

    static Stream<Arguments> runtimeErrors() {
        return Stream.of(
                Arguments.of("division by zero", "int main() { int z = 0; print_int(1 / z); }"),
                Arguments.of("modulo by zero", "int main() { int z = 0; print_int(1 % z); }"),
                Arguments.of("stack overflow", "int f(int n) { return f(n + 1); } int main() { return f(0); }"),
                Arguments.of("null pointer dereference", "int main() { int* p; return *p; }"),
                Arguments.of("null pointer dereference",
                        "class A { public: int v; }; int main() { A* a = nullptr; return a->v; }"),
                Arguments.of("null pointer dereference",
                        "class A { public: void m() { } }; int main() { A* a; a->m(); }"),
                Arguments.of("dangling pointer", "int* f() { int x = 1; return &x; } int main() { return *f(); }"),
                Arguments.of("dangling pointer", "int* f(int x) { return &x; } int main() { return *f(1); }"),
                Arguments.of("dangling pointer", "int main() { int* p; { int x = 1; p = &x; } return *p; }"),
                Arguments.of("dangling pointer",
                        "class A { public: int v; }; int main() { int* p; { A a; p = &a.v; } return *p; }"),
                Arguments.of("use of deleted memory", "int main() { int* p = new int(1); delete p; return *p; }"),
                Arguments.of("use of deleted memory",
                        "int main() { int* p = new int(1); int& r = *p; delete p; return r; }"),
                Arguments.of("double delete", "int main() { int* p = new int; delete p; delete p; }"),
                Arguments.of("not obtained from 'new'", "int main() { int x; delete &x; }"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("runtimeErrors")
    void failsAtRuntime(String message, String source) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        MiniCpp.RunResult result = MiniCpp.run(source, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        assertEquals(1, result.diagnostics().size(), () -> result.diagnostics().toString());
        assertEquals(Phase.RUNTIME, result.diagnostics().get(0).phase());
        assertTrue(result.diagnostics().get(0).message().contains(message));
        assertEquals(1, result.exitCode());
    }

    @Test
    void outputBeforeRuntimeErrorIsKept() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        MiniCpp.RunResult result = MiniCpp.run("int main() { print_int(1); int z = 0; print_int(1 / z); }",
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        assertEquals("1\n", buffer.toString(StandardCharsets.UTF_8));
        assertEquals("1:49: runtime error: division by zero", result.diagnostics().get(0).format());
    }
}
