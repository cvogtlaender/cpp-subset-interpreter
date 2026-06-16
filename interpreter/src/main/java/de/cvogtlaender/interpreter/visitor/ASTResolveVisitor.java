package de.cvogtlaender.interpreter.visitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.ConstructorDecl;
import de.cvogtlaender.interpreter.ast.declaration.Decl;
import de.cvogtlaender.interpreter.ast.declaration.FieldDecl;
import de.cvogtlaender.interpreter.ast.declaration.FunctionDecl;
import de.cvogtlaender.interpreter.ast.declaration.MethodDecl;
import de.cvogtlaender.interpreter.ast.declaration.ParameterDecl;
import de.cvogtlaender.interpreter.ast.declaration.VariableDecl;
import de.cvogtlaender.interpreter.ast.expression.AssignExpr;
import de.cvogtlaender.interpreter.ast.expression.BinaryExpr;
import de.cvogtlaender.interpreter.ast.expression.BoolLiteral;
import de.cvogtlaender.interpreter.ast.expression.CallExpr;
import de.cvogtlaender.interpreter.ast.expression.CharLiteral;
import de.cvogtlaender.interpreter.ast.expression.ErrorExpr;
import de.cvogtlaender.interpreter.ast.expression.Expr;
import de.cvogtlaender.interpreter.ast.expression.IntLiteral;
import de.cvogtlaender.interpreter.ast.expression.MemberAccessExpr;
import de.cvogtlaender.interpreter.ast.expression.StringLiteral;
import de.cvogtlaender.interpreter.ast.expression.UnaryExpr;
import de.cvogtlaender.interpreter.ast.expression.VarExpr;
import de.cvogtlaender.interpreter.ast.statement.BlockStmt;
import de.cvogtlaender.interpreter.ast.statement.ExprStmt;
import de.cvogtlaender.interpreter.ast.statement.IfStmt;
import de.cvogtlaender.interpreter.ast.statement.ReturnStmt;
import de.cvogtlaender.interpreter.ast.statement.Stmt;
import de.cvogtlaender.interpreter.ast.statement.VariableStmt;
import de.cvogtlaender.interpreter.ast.statement.WhileStmt;
import de.cvogtlaender.interpreter.ast.type.ClassType;
import de.cvogtlaender.interpreter.ast.type.PointerType;
import de.cvogtlaender.interpreter.ast.type.PrimitiveType;
import de.cvogtlaender.interpreter.ast.type.ReferenceType;
import de.cvogtlaender.interpreter.ast.type.Type;

public class ASTResolveVisitor implements AstVisitor<Void> {

  private final Map<String, ClassDecl> classes = new LinkedHashMap<>();
  private final Map<String, List<FunctionDecl>> functions = new LinkedHashMap<>();
  private final Deque<Map<String, Decl>> localScopes = new ArrayDeque<>();

  private final List<String> errors = new ArrayList<>();

  public Map<String, ClassDecl> getClasses() {
    return classes;
  }

  public Map<String, List<FunctionDecl>> getFunctions() {
    return functions;
  }

  public List<String> getErrors() {
    return errors;
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public void resolve(Program program) {
    collectTopLevel(program);
    visitProgram(program);
  }

  private void collectTopLevel(Program program) {
    for (ClassDecl c : program.getClassDefs()) {
      if (classes.containsKey(c.getClassName())) {
        errors.add("Duplicate class name '" + c.getClassName() + "'");
      } else {
        classes.put(c.getClassName(), c);
      }
    }
    for (FunctionDecl f : program.getFunctions()) {
      functions.computeIfAbsent(f.getName(), k -> new ArrayList<>()).add(f);
    }
  }

  @Override
  public Void visitProgram(Program node) {
    for (ClassDecl c : node.getClassDefs()) {
      c.accept(this);
    }
    for (FunctionDecl f : node.getFunctions()) {
      f.accept(this);
    }
    return null;
  }

  @Override
  public Void visitFunctionDecl(FunctionDecl node) {
    validateType(node.getReturnType(), "return type of '" + node.getName() + "'");
    pushScope();
    for (ParameterDecl p : node.getParameters()) {
      validateType(p.getType(), "parameter '" + p.getName() + "'");
      define(p.getName(), p);
    }
    visitBlockStmt(node.getBody());
    popScope();
    return null;
  }

  @Override
  public Void visitClassDecl(ClassDecl node) {
    if (node.getParentClassName() != null && !classes.containsKey(node.getParentClassName())) {
      errors.add("Unknown base class '" + node.getParentClassName() + "' in class '" + node.getClassName() + "'");
    }
    for (FieldDecl f : node.getFields()) {
      validateType(f.getType(), "field '" + f.getName() + "'");
    }
    for (ConstructorDecl c : node.getConstructors()) {
      c.accept(this);
    }
    for (MethodDecl m : node.getMethods()) {
      m.accept(this);
    }
    return null;
  }

  @Override
  public Void visitConstructorDecl(ConstructorDecl node) {
    pushScope();
    for (ParameterDecl p : node.getParameters()) {
      validateType(p.getType(), "parameter '" + p.getName() + "'");
      define(p.getName(), p);
    }
    visitBlockStmt(node.getBody());
    popScope();
    return null;
  }

  @Override
  public Void visitMethodDecl(MethodDecl node) {
    validateType(node.getReturnType(), "return type of '" + node.getName() + "'");
    pushScope();
    for (ParameterDecl p : node.getParameters()) {
      validateType(p.getType(), "parameter '" + p.getName() + "'");
      define(p.getName(), p);
    }
    visitBlockStmt(node.getBody());
    popScope();
    return null;
  }

  @Override
  public Void visitBlockStmt(BlockStmt node) {
    pushScope();
    for (Stmt stmt : node.getStatements()) {
      stmt.accept(this);
    }
    popScope();
    return null;
  }

  @Override
  public Void visitVariableStmt(VariableStmt node) {
    VariableDecl decl = node.getVariableDecl();
    validateType(decl.getType(), "variable '" + decl.getName() + "'");
    if (decl.getInitializer() != null) {
      decl.getInitializer().accept(this);
    }
    define(decl.getName(), decl);
    return null;
  }

  @Override
  public Void visitIfStmt(IfStmt node) {
    node.getCondition().accept(this);
    node.getIfBranch().accept(this);
    if (node.getElseBranch() != null) {
      node.getElseBranch().accept(this);
    }
    return null;
  }

  @Override
  public Void visitWhileStmt(WhileStmt node) {
    node.getCondition().accept(this);
    node.getBody().accept(this);
    return null;
  }

  @Override
  public Void visitReturnStmt(ReturnStmt node) {
    if (node.getReturnValue() != null) {
      node.getReturnValue().accept(this);
    }
    return null;
  }

  @Override
  public Void visitExprStmt(ExprStmt node) {
    node.getExpression().accept(this);
    return null;
  }

  @Override
  public Void visitVarExpr(VarExpr node) {
    String name = node.getName();
    Decl local = lookupLocal(name);
    if (local != null) {
      node.setResolvedDecl(local);
    } else if (classes.containsKey(name)) {
      node.setResolvedDecl(classes.get(name));
    } else if (functions.containsKey(name)) {
      // pass
    } else {
      errors.add("Undefined identifier '" + name + "'");
    }
    return null;
  }

  @Override
  public Void visitAssignExpr(AssignExpr node) {
    node.getTarget().accept(this);
    node.getValue().accept(this);
    return null;
  }

  @Override
  public Void visitBinaryExpr(BinaryExpr node) {
    node.getLeftHandSide().accept(this);
    node.getRightHandSide().accept(this);
    return null;
  }

  @Override
  public Void visitUnaryExpr(UnaryExpr node) {
    node.getExpr().accept(this);
    return null;
  }

  @Override
  public Void visitCallExpr(CallExpr node) {
    node.getCallee().accept(this);
    for (Expr arg : node.getArguments()) {
      arg.accept(this);
    }
    return null;
  }

  @Override
  public Void visitMemberAccessExpr(MemberAccessExpr node) {
    node.getObj().accept(this);
    return null;
  }

  @Override
  public Void visitIntLiteral(IntLiteral node) {
    return null;
  }

  @Override
  public Void visitBoolLiteral(BoolLiteral node) {
    return null;
  }

  @Override
  public Void visitCharLiteral(CharLiteral node) {
    return null;
  }

  @Override
  public Void visitStringLiteral(StringLiteral node) {
    return null;
  }

  @Override
  public Void visitErrorExpr(ErrorExpr node) {
    return null;
  }

  @Override
  public Void visitFieldDecl(FieldDecl node) {
    return null;
  }

  @Override
  public Void visitParameterDecl(ParameterDecl node) {
    return null;
  }

  @Override
  public Void visitVariableDecl(VariableDecl node) {
    return null;
  }

  @Override
  public Void visitClassType(ClassType node) {
    return null;
  }

  @Override
  public Void visitPrimitiveType(PrimitiveType node) {
    return null;
  }

  @Override
  public Void visitPointerType(PointerType node) {
    return null;
  }

  @Override
  public Void visitReferenceType(ReferenceType node) {
    return null;
  }

  private void pushScope() {
    localScopes.push(new LinkedHashMap<>());
  }

  private void popScope() {
    localScopes.pop();
  }

  private void define(String name, Decl decl) {
    if (!localScopes.isEmpty()) {
      localScopes.peek().put(name, decl);
    }
  }

  private void validateType(Type type, String context) {
    if (type instanceof ClassType ct) {
      if (!classes.containsKey(ct.getName())) {
        errors.add("Unknown type '" + ct.getName() + "' in " + context);
      }
    } else if (type instanceof ReferenceType rt) {
      validateType(rt.getReferencedType(), context);
    } else if (type instanceof PointerType pt) {
      validateType(pt.getPointeeType(), context);
    }
  }

  private Decl lookupLocal(String name) {
    for (Map<String, Decl> scope : localScopes) {
      if (scope.containsKey(name)) {
        return scope.get(name);
      }
    }
    return null;
  }
}
