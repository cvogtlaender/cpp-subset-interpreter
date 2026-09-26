package de.cvogtlaender.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.FunctionDecl;
import de.cvogtlaender.interpreter.ast.declaration.VariableDecl;
import de.cvogtlaender.interpreter.ast.expression.BinaryExpr;
import de.cvogtlaender.interpreter.ast.expression.CallExpr;
import de.cvogtlaender.interpreter.ast.expression.CharLiteral;
import de.cvogtlaender.interpreter.ast.expression.StringLiteral;
import de.cvogtlaender.interpreter.ast.expression.UnaryExpr;
import de.cvogtlaender.interpreter.ast.expression.VarExpr;
import de.cvogtlaender.interpreter.ast.statement.ReturnStmt;
import de.cvogtlaender.interpreter.ast.statement.VariableStmt;
import de.cvogtlaender.interpreter.ast.type.ReferenceType;

class ParserTest {

  private static Program parse(String source) {
    MiniCpp.ParseResult<Program> result = MiniCpp.parseProgram(source);
    assertTrue(result.diagnostics().isEmpty(), () -> result.diagnostics().toString());
    return result.tree();
  }

  private static VariableDecl firstVariable(Program program) {
    return ((VariableStmt) program.getFunctions().get(0).getBody().getStatements().get(0)).getVariableDecl();
  }

  @Test
  void precedenceFollowsCpp() {
    VariableDecl decl = firstVariable(parse("int main() { bool b = 1 + 2 * 3 < 4 || !true && false; }"));
    BinaryExpr or = (BinaryExpr) decl.getInitializer();
    assertEquals(BinaryExpr.Operator.OR, or.getOperator());
    BinaryExpr lt = (BinaryExpr) or.getLeftHandSide();
    assertEquals(BinaryExpr.Operator.LT, lt.getOperator());
    BinaryExpr plus = (BinaryExpr) lt.getLeftHandSide();
    assertEquals(BinaryExpr.Operator.ADD, plus.getOperator());
    assertEquals(BinaryExpr.Operator.MUL, ((BinaryExpr) plus.getRightHandSide()).getOperator());
    BinaryExpr and = (BinaryExpr) or.getRightHandSide();
    assertEquals(BinaryExpr.Operator.AND, and.getOperator());
    assertEquals(UnaryExpr.Operator.NOT, ((UnaryExpr) and.getLeftHandSide()).getOperator());
  }

  @Test
  void unaryPlusIsSupported() {
    VariableDecl decl = firstVariable(parse("int main() { int x = +1; }"));
    assertEquals(UnaryExpr.Operator.POSITIVE, ((UnaryExpr) decl.getInitializer()).getOperator());
  }

  @Test
  void literalsAreUnescaped() {
    VariableDecl s = firstVariable(parse("int main() { string s = \"a\\tb\\\"c\\n\"; }"));
    assertEquals("a\tb\"c\n", ((StringLiteral) s.getInitializer()).getValue());
    VariableDecl c = firstVariable(parse("int main() { char c = '\\n'; }"));
    assertEquals('\n', ((CharLiteral) c.getInitializer()).getValue());
    VariableDecl q = firstVariable(parse("int main() { char c = 'q'; }"));
    assertEquals('q', ((CharLiteral) q.getInitializer()).getValue());
  }

  @Test
  void returnWithoutValue() {
    Program p = parse("void main() { return; }");
    ReturnStmt r = (ReturnStmt) p.getFunctions().get(0).getBody().getStatements().get(0);
    assertNull(r.getReturnValue());
  }

  @Test
  void directInitializationIsSugarForConstructorCall() {
    VariableDecl decl = firstVariable(parse("int main() { A a(1, 2); }"));
    CallExpr call = assertInstanceOf(CallExpr.class, decl.getInitializer());
    assertEquals("A", ((VarExpr) call.getCallee()).getName());
    assertEquals(2, call.getArguments().size());
  }

  @Test
  void referenceTypes() {
    Program p = parse("void f(int& a) { int& b = a; } int main() { }");
    FunctionDecl f = p.getFunctions().get(0);
    assertInstanceOf(ReferenceType.class, f.getParameters().get(0).getType());
    assertEquals("int&", firstVariable(p).getType().getName());
  }

  @Test
  void classesKeepMembers() {
    Program p = parse("""
        class B { public: int x; };
        class D : public B {
        public:
          D() { }
          D(int v) { x = v; }
          virtual int get() { return x; }
          void set(int v) { x = v; }
        };
        int main() { }
        """);
    ClassDecl d = p.getClassDefs().get(1);
    assertEquals("B", d.getParentClassName());
    assertEquals(2, d.getConstructors().size());
    assertEquals(2, d.getMethods().size());
    assertTrue(d.getMethods().get(0).getIsVirtual());
  }

  @Test
  void nodesCarryPositions() {
    Program p = parse("int main() {\n  int value = 1 + 2;\n}");
    VariableDecl decl = firstVariable(p);
    assertEquals(2, decl.getLine());
    assertEquals(2, decl.getColumn());
    BinaryExpr plus = (BinaryExpr) decl.getInitializer();
    assertEquals(2, plus.getLine());
    assertEquals(14, plus.getColumn());
    assertEquals(19, plus.getEndColumn());
  }

  @Test
  void syntaxErrorsAreCollected() {
    MiniCpp.ParseResult<Program> result = MiniCpp.parseProgram("int main() { int = 3; }");
    assertNull(result.tree());
    assertEquals(1, result.diagnostics().get(0).line());
  }

  @Test
  void incompleteInputIsDetected() {
    assertTrue(MiniCpp.parseReplInput("int f() {").incomplete());
    assertTrue(MiniCpp.parseReplInput("1 +").incomplete());
    assertTrue(!MiniCpp.parseReplInput("int 5;").incomplete());
  }
}
