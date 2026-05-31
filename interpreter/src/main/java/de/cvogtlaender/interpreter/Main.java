package de.cvogtlaender.interpreter;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import de.cvogtlaender.interpreter.ast.Program;

public class Main {

  public static void main(String[] args) {

    String featureCpp = "// Präprozessor-Direktive (wird wie Kommentar ignoriert)\r\n" + //
        "#include <iostream>\r\n" + //
        "\r\n" + //
        "/*\r\n" + //
        "   Block-Kommentar:\r\n" + //
        "   Dieses Programm demonstriert:\r\n" + //
        "   - Primitive Typen\r\n" + //
        "   - Ausdrücke & Operatoren\r\n" + //
        "   - Funktionen + Overloading\r\n" + //
        "   - Referenzen\r\n" + //
        "   - Klassen + Vererbung + Polymorphie\r\n" + //
        "   - Kontrollfluss\r\n" + //
        "*/\r\n" + //
        "\r\n" + //
        "// ---------- Funktionen (inkl. Overloading) ----------\r\n" + //
        "\r\n" + //
        "// einfache Funktion\r\n" + //
        "int add(int a, int b) {\r\n" + //
        "    return a + b;\r\n" + //
        "}\r\n" + //
        "\r\n" + //
        "// Overloading (gleicher Name, andere Signatur)\r\n" + //
        "int add(int a, int b, int c) {\r\n" + //
        "    return a + b + c;\r\n" + //
        "}\r\n" + //
        "\r\n" + //
        "// Funktion mit Referenzparameter\r\n" + //
        "void increment(int& x) {\r\n" + //
        "    x = x + 1; // schreibt direkt ins Original (Referenz)\r\n" + //
        "}\r\n" + //
        "\r\n" + //
        "// ---------- Klassen ----------\r\n" + //
        "\r\n" + //
        "// Basisklasse\r\n" + //
        "class Base {\r\n" + //
        "public:\r\n" + //
        "    int value;\r\n" + //
        "\r\n" + //
        "    // Default-Konstruktor\r\n" + //
        "    Base() {\r\n" + //
        "        value = 10;\r\n" + //
        "    }\r\n" + //
        "\r\n" + //
        "    // Konstruktor mit Argument\r\n" + //
        "    Base(int v) {\r\n" + //
        "        value = v;\r\n" + //
        "    }\r\n" + //
        "\r\n" + //
        "    // virtuelle Methode\r\n" + //
        "    virtual void print() {\r\n" + //
        "        print_string(\"Base::print -> \");\r\n" + //
        "        print_int(value);\r\n" + //
        "    }\r\n" + //
        "};\r\n" + //
        "\r\n" + //
        "// Abgeleitete Klasse\r\n" + //
        "class Derived : public Base {\r\n" + //
        "public:\r\n" + //
        "    int extra;\r\n" + //
        "\r\n" + //
        "    // Default-Konstruktor\r\n" + //
        "    Derived() {\r\n" + //
        "        extra = 100;\r\n" + //
        "    }\r\n" + //
        "\r\n" + //
        "    // Konstruktor mit Argumenten\r\n" + //
        "    Derived(int v, int e) {\r\n" + //
        "        value = v;   // geerbtes Feld\r\n" + //
        "        extra = e;\r\n" + //
        "    }\r\n" + //
        "\r\n" + //
        "    // Überschreiben (virtual implizit)\r\n" + //
        "    void print() {\r\n" + //
        "        print_string(\"Derived::print -> \");\r\n" + //
        "        print_int(value);\r\n" + //
        "        print_string(\", \");\r\n" + //
        "        print_int(extra);\r\n" + //
        "    }\r\n" + //
        "};\r\n" + //
        "\r\n" + //
        "// Klasse mit Methoden und Feldern\r\n" + //
        "class Counter {\r\n" + //
        "public:\r\n" + //
        "    int count;\r\n" + //
        "\r\n" + //
        "    Counter() {\r\n" + //
        "        count = 0;\r\n" + //
        "    }\r\n" + //
        "\r\n" + //
        "    void inc() {\r\n" + //
        "        count = count + 1;\r\n" + //
        "    }\r\n" + //
        "\r\n" + //
        "    int get() {\r\n" + //
        "        return count;\r\n" + //
        "    }\r\n" + //
        "};\r\n" + //
        "\r\n" + //
        "// ---------- main ----------\r\n" + //
        "\r\n" + //
        "int main() {\r\n" + //
        "\r\n" + //
        "    // ----- Primitive Typen -----\r\n" + //
        "    int a = 5;\r\n" + //
        "    int b = 2;\r\n" + //
        "    bool flag = true;\r\n" + //
        "    char c = 'x';\r\n" + //
        "    string s = \"hello\";\r\n" + //
        "\r\n" + //
        "    // Escape-Sequenz\r\n" + //
        "    char nullChar = '\\0';\r\n" + //
        "\r\n" + //
        "    // ----- Arithmetik -----\r\n" + //
        "    int sum = a + b;\r\n" + //
        "    int diff = a - b;\r\n" + //
        "    int prod = a * b;\r\n" + //
        "    int div = a / b;\r\n" + //
        "    int mod = a % b;\r\n" + //
        "\r\n" + //
        "    // ----- Vergleich -----\r\n" + //
        "    bool cmp1 = (a == b);\r\n" + //
        "    bool cmp2 = (a != b);\r\n" + //
        "    bool cmp3 = (a < b);\r\n" + //
        "\r\n" + //
        "    // ----- Logik (Short-Circuit) -----\r\n" + //
        "    bool logic = flag && (a > 0) || false;\r\n" + //
        "\r\n" + //
        "    // ----- if / else -----\r\n" + //
        "    if (a > b) {\r\n" + //
        "        print_string(\"a > b\\n\");\r\n" + //
        "    } else {\r\n" + //
        "        print_string(\"a <= b\\n\");\r\n" + //
        "    }\r\n" + //
        "\r\n" + //
        "    // ----- while -----\r\n" + //
        "    int i = 0;\r\n" + //
        "    while (i < 3) {\r\n" + //
        "        print_string(\"Loop i = \");\r\n" + //
        "        print_int(i);\r\n" + //
        "        print_string(\"\\n\");\r\n" + //
        "        i = i + 1;\r\n" + //
        "    }\r\n" + //
        "\r\n" + //
        "    // ----- Funktionen -----\r\n" + //
        "    int r1 = add(1, 2);\r\n" + //
        "    int r2 = add(1, 2, 3);\r\n" + //
        "\r\n" + //
        "    print_string(\"add results: \");\r\n" + //
        "    print_int(r1);\r\n" + //
        "    print_string(\", \");\r\n" + //
        "    print_int(r2);\r\n" + //
        "    print_string(\"\\n\");\r\n" + //
        "\r\n" + //
        "    // ----- Referenzen -----\r\n" + //
        "    int x = 10;\r\n" + //
        "    int& ref = x;   // Referenz muss initialisiert werden\r\n" + //
        "    ref = 20;       // schreibt in x\r\n" + //
        "\r\n" + //
        "    increment(x);   // Referenzparameter\r\n" + //
        "\r\n" + //
        "    print_string(\"x after increment: \");\r\n" + //
        "    print_int(x);\r\n" + //
        "    print_string(\"\\n\");\r\n" + //
        "\r\n" + //
        "    // ----- Klassenobjekte -----\r\n" + //
        "    Base b1;               // Default-Konstruktor\r\n" + //
        "    Base b2 = Base(42);   // Konstruktor mit Argument\r\n" + //
        "\r\n" + //
        "    b1.print();\r\n" + //
        "    b2.print();\r\n" + //
        "\r\n" + //
        "    // ----- Vererbung -----\r\n" + //
        "    Derived d1;\r\n" + //
        "    Derived d2 = Derived(7, 99);\r\n" + //
        "\r\n" + //
        "    d1.print();\r\n" + //
        "    d2.print();\r\n" + //
        "\r\n" + //
        "    // ----- Polymorphie über Referenz -----\r\n" + //
        "    Base& refBase = d2;  // Referenz auf Base zeigt auf Derived\r\n" + //
        "    refBase.print();     // dynamischer Dispatch!\r\n" + //
        "\r\n" + //
        "    // ----- Slicing -----\r\n" + //
        "    Base b3;\r\n" + //
        "    b3 = d2;  // slicing: extra geht verloren\r\n" + //
        "    b3.print();\r\n" + //
        "\r\n" + //
        "    // ----- Objekt mit Methoden -----\r\n" + //
        "    Counter ctr;\r\n" + //
        "    ctr.inc();\r\n" + //
        "    ctr.inc();\r\n" + //
        "\r\n" + //
        "    print_string(\"Counter: \");\r\n" + //
        "    print_int(ctr.get());\r\n" + //
        "    print_string(\"\\n\");\r\n" + //
        "\r\n" + //
        "    // ----- Feldzugriff -----\r\n" + //
        "    print_string(\"Derived.extra = \");\r\n" + //
        "    print_int(d2.extra);\r\n" + //
        "    print_string(\"\\n\");\r\n" + //
        "\r\n" + //
        "    return 0;\r\n" + //
        "}";

    MiniCppLexer lexer = new MiniCppLexer(CharStreams.fromString(featureCpp));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    MiniCppParser parser = new MiniCppParser(tokens);

    ASTVisitor astVisitor = new ASTVisitor();

    Program prog = (Program) astVisitor.visitProgram(parser.program());

    System.out.println(prog.toStringTree());
  }

}
