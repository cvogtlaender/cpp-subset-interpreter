package de.cvogtlaender.interpreter.visitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import de.cvogtlaender.interpreter.ast.expression.NewExpr;
import de.cvogtlaender.interpreter.ast.expression.NullptrLiteral;
import de.cvogtlaender.interpreter.ast.expression.StringLiteral;
import de.cvogtlaender.interpreter.ast.expression.UnaryExpr;
import de.cvogtlaender.interpreter.ast.expression.VarExpr;
import de.cvogtlaender.interpreter.ast.statement.BlockStmt;
import de.cvogtlaender.interpreter.ast.statement.DeleteStmt;
import de.cvogtlaender.interpreter.ast.statement.ExprStmt;
import de.cvogtlaender.interpreter.ast.statement.IfStmt;
import de.cvogtlaender.interpreter.ast.statement.ReturnStmt;
import de.cvogtlaender.interpreter.ast.statement.Stmt;
import de.cvogtlaender.interpreter.ast.statement.VariableStmt;
import de.cvogtlaender.interpreter.ast.statement.WhileStmt;
import de.cvogtlaender.interpreter.ast.type.ClassType;
import de.cvogtlaender.interpreter.ast.type.NullptrType;
import de.cvogtlaender.interpreter.ast.type.PointerType;
import de.cvogtlaender.interpreter.ast.type.PrimitiveType;
import de.cvogtlaender.interpreter.ast.type.ReferenceType;
import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.diagnostic.Diagnostic;
import de.cvogtlaender.interpreter.semantic.GlobalScope;
import de.cvogtlaender.interpreter.semantic.Types;
import de.cvogtlaender.interpreter.ast.AstNode;

public class ASTResolveVisitor implements AstVisitor<Void> {

  private final GlobalScope globals;
  private final Deque<Map<String, Decl>> localScopes = new ArrayDeque<>();
  private final List<Diagnostic> diagnostics = new ArrayList<>();
  private ClassDecl currentClass;

  public ASTResolveVisitor() {
    this(new GlobalScope());
  }

  public ASTResolveVisitor(GlobalScope globals) {
    this.globals = globals;
  }

  public GlobalScope getGlobals() {
    return globals;
  }

  public Map<String, ClassDecl> getClasses() {
    return globals.getClasses();
  }

  public Map<String, List<FunctionDecl>> getFunctions() {
    return globals.getFunctions();
  }

  public List<Diagnostic> getDiagnostics() {
    return diagnostics;
  }

  public List<String> getErrors() {
    return diagnostics.stream().map(Diagnostic::format).toList();
  }

  public boolean hasErrors() {
    return !diagnostics.isEmpty();
  }

  public void resolve(Program program) {
    for (ClassDecl c : program.getClassDefs()) {
      collectClass(c);
    }
    for (FunctionDecl f : program.getFunctions()) {
      collectFunction(f);
    }
    for (ClassDecl c : program.getClassDefs()) {
      linkParent(c);
    }
    for (ClassDecl c : program.getClassDefs()) {
      checkMembers(c);
    }

    visitProgram(program);
  }

  public void declareClass(ClassDecl c) {
    int before = diagnostics.size();
    collectClass(c);
    if (diagnostics.size() > before) {
      return;
    }
    linkParent(c);
    checkMembers(c);
    c.accept(this);
  }

  public void declareFunction(FunctionDecl f) {
    collectFunction(f);
    f.accept(this);
  }

  public void resolveSessionStatement(Stmt stmt) {
    localScopes.push(globals.getSession());
    try {
      stmt.accept(this);
    } finally {
      localScopes.pop();
    }
  }

  public void resolveSessionExpr(Expr expr) {
    localScopes.push(globals.getSession());
    try {
      expr.accept(this);
    } finally {
      localScopes.pop();
    }
  }

  private void collectClass(ClassDecl c) {
    String name = c.getClassName();
    if (globals.getClasses().containsKey(name)) {
      error(c, "redefinition of class '" + name + "'");
      return;
    }
    if (globals.getFunctions().containsKey(name)) {
      error(c, "class '" + name + "' conflicts with a function of the same name");
      return;
    }
    globals.getClasses().put(name, c);
  }

  private void collectFunction(FunctionDecl f) {
    if (globals.getClasses().containsKey(f.getName())) {
      error(f, "function '" + f.getName() + "' conflicts with a class of the same name");
      return;
    }
    List<FunctionDecl> overloads = globals.getFunctions().computeIfAbsent(f.getName(), k -> new ArrayList<>());
    String key = Types.parameterKey(f.getParameters());
    for (FunctionDecl other : overloads) {
      if (Types.parameterKey(other.getParameters()).equals(key)) {
        String what = other.isBuiltin() ? "built-in function" : "function";
        error(f, "redefinition of " + what + " '" + Types.signature(f.getName(), f.getParameters()) + "'");
        return;
      }
    }
    overloads.add(f);
  }

  private void linkParent(ClassDecl c) {
    String parentName = c.getParentClassName();
    if (parentName == null) {
      return;
    }
    ClassDecl parent = globals.getClass(parentName);
    if (parent == null) {
      error(c, "unknown base class '" + parentName + "' of class '" + c.getClassName() + "'");
      return;
    }
    Set<ClassDecl> seen = new HashSet<>();
    seen.add(c);
    for (ClassDecl p = parent; p != null; p = globals
        .getClass(p.getParentClassName() == null ? "" : p.getParentClassName())) {
      if (!seen.add(p)) {
        error(c, "class '" + c.getClassName() + "' inherits from itself");
        return;
      }
    }
    c.setParent(parent);
  }

  private void checkMembers(ClassDecl c) {
    Set<String> names = new HashSet<>();

    for (FieldDecl f : c.getFields()) {
      f.setOwner(c);
      if (!names.add(f.getName())) {
        error(f, "duplicate member '" + f.getName() + "' in class '" + c.getClassName() + "'");
      } else if (c.getParent() != null && GlobalScope.findField(c.getParent(), f.getName()) != null) {
        error(f, "field '" + f.getName() + "' is already declared in a base class of '" + c.getClassName() + "'");
      }
    }

    Map<String, MethodDecl> methodSignatures = new LinkedHashMap<>();
    for (MethodDecl m : c.getMethods()) {
      m.setOwner(c);
      if (m.getName().equals(c.getClassName())) {
        error(m, "method must not have the same name as its class '" + c.getClassName() + "'");
      }
      boolean isField = c.getFields().stream().anyMatch(f -> f.getName().equals(m.getName()));
      if (isField) {
        error(m, "method '" + m.getName() + "' conflicts with a field of the same name");
      }
      String key = m.getName() + "(" + Types.parameterKey(m.getParameters()) + ")";
      if (methodSignatures.putIfAbsent(key, m) != null) {
        error(m, "redefinition of method '" + Types.signature(m.getName(), m.getParameters()) + "'");
      }
    }

    Set<String> constructorSignatures = new HashSet<>();
    for (ConstructorDecl ctor : c.getConstructors()) {
      ctor.setOwner(c);
      if (!ctor.getName().equals(c.getClassName())) {
        error(ctor, "constructor '" + ctor.getName() + "' must be named like its class '" + c.getClassName()
            + "' (or is a method missing its return type)");
      }
      if (!constructorSignatures.add(Types.parameterKey(ctor.getParameters()))) {
        error(ctor, "redefinition of constructor '" + Types.signature(c.getClassName(), ctor.getParameters()) + "'");
      }
    }

    if (c.getConstructors().isEmpty()) {
      ConstructorDecl synthesized = new ConstructorDecl(c.getClassName(), new ArrayList<>(),
          new BlockStmt(new ArrayList<>()));
      synthesized.copyRange(c);
      synthesized.getBody().copyRange(c);
      synthesized.setOwner(c);
      synthesized.setSynthesized(true);
      c.getConstructors().add(synthesized);
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
    validateType(node.getReturnType(), node, true);
    resolveCallable(node.getParameters(), node.getBody());
    return null;
  }

  @Override
  public Void visitClassDecl(ClassDecl node) {
    ClassDecl previous = currentClass;
    currentClass = node;
    try {
      for (FieldDecl f : node.getFields()) {
        f.accept(this);
      }
      for (ConstructorDecl c : node.getConstructors()) {
        c.accept(this);
      }
      for (MethodDecl m : node.getMethods()) {
        m.accept(this);
      }
    } finally {
      currentClass = previous;
    }
    return null;
  }

  @Override
  public Void visitConstructorDecl(ConstructorDecl node) {
    resolveCallable(node.getParameters(), node.getBody());
    return null;
  }

  @Override
  public Void visitMethodDecl(MethodDecl node) {
    validateType(node.getReturnType(), node, true);
    resolveCallable(node.getParameters(), node.getBody());
    return null;
  }

  @Override
  public Void visitFieldDecl(FieldDecl node) {
    validateType(node.getType(), node, false);
    return null;
  }

  private void resolveCallable(List<ParameterDecl> parameters, BlockStmt body) {
    Deque<Map<String, Decl>> saved = new ArrayDeque<>(localScopes);
    localScopes.clear();
    pushScope();
    try {
      for (ParameterDecl p : parameters) {
        p.accept(this);
      }
      for (Stmt stmt : body.getStatements()) {
        stmt.accept(this);
      }
    } finally {
      localScopes.clear();
      localScopes.addAll(saved);
    }
  }

  @Override
  public Void visitParameterDecl(ParameterDecl node) {
    validateType(node.getType(), node, false);
    define(node.getName(), node, node);
    return null;
  }

  @Override
  public Void visitBlockStmt(BlockStmt node) {
    pushScope();
    try {
      for (Stmt stmt : node.getStatements()) {
        stmt.accept(this);
      }
    } finally {
      popScope();
    }
    return null;
  }

  @Override
  public Void visitVariableStmt(VariableStmt node) {
    node.getVariableDecl().accept(this);
    return null;
  }

  @Override
  public Void visitVariableDecl(VariableDecl node) {
    validateType(node.getType(), node, false);
    if (node.getInitializer() != null) {
      node.getInitializer().accept(this);
    }
    define(node.getName(), node, node);
    return null;
  }

  @Override
  public Void visitIfStmt(IfStmt node) {
    node.getCondition().accept(this);
    resolveBranch(node.getIfBranch());
    if (node.getElseBranch() != null) {
      resolveBranch(node.getElseBranch());
    }
    return null;
  }

  @Override
  public Void visitWhileStmt(WhileStmt node) {
    node.getCondition().accept(this);
    resolveBranch(node.getBody());
    return null;
  }

  private void resolveBranch(Stmt branch) {
    pushScope();
    try {
      branch.accept(this);
    } finally {
      popScope();
    }
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
  public Void visitDeleteStmt(DeleteStmt node) {
    node.getPointer().accept(this);
    return null;
  }

  @Override
  public Void visitVarExpr(VarExpr node) {
    String name = node.getName();

    Decl local = lookupLocal(name);
    if (local != null) {
      node.setKind(VarExpr.Kind.VARIABLE);
      node.setResolvedDecl(local);
      return null;
    }

    if (currentClass != null) {
      ClassDecl owner = GlobalScope.findMemberOwner(currentClass, name);
      if (owner != null) {
        FieldDecl field = GlobalScope.findField(owner, name);
        node.setMemberOwner(owner);
        if (field != null && field.getOwner() == owner) {
          node.setKind(VarExpr.Kind.FIELD);
          node.setResolvedDecl(field);
        } else {
          node.setKind(VarExpr.Kind.METHOD);
        }
        return null;
      }
    }

    if (globals.getFunctions().containsKey(name)) {
      node.setKind(VarExpr.Kind.FUNCTION);
      return null;
    }

    ClassDecl cls = globals.getClass(name);
    if (cls != null) {
      node.setKind(VarExpr.Kind.CLASS);
      node.setResolvedDecl(cls);
      return null;
    }

    boolean inSession = localScopes.stream().anyMatch(scope -> scope == globals.getSession());
    if (globals.getSession().containsKey(name) && !inSession) {
      error(node, "session variable '" + name + "' cannot be used inside a function");
    } else {
      error(node, "use of undeclared identifier '" + name + "'");
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
  public Void visitNewExpr(NewExpr node) {
    if (Types.isVoid(node.getAllocatedType())) {
      error(node, "cannot allocate an object of type 'void'");
    } else {
      validateType(node.getAllocatedType(), node, false);
    }
    for (Expr arg : node.getArguments()) {
      arg.accept(this);
    }
    return null;
  }

  @Override
  public Void visitNullptrLiteral(NullptrLiteral node) {
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
  public Void visitClassType(ClassType node) {
    return null;
  }

  @Override
  public Void visitPrimitiveType(PrimitiveType node) {
    return null;
  }

  @Override
  public Void visitNullptrType(NullptrType node) {
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

  private void define(String name, Decl decl, AstNode at) {
    if (localScopes.isEmpty()) {
      return;
    }
    Map<String, Decl> scope = localScopes.peek();
    if (scope.containsKey(name) && scope != globals.getSession()) {
      error(at, "redeclaration of '" + name + "'");
      return;
    }
    scope.put(name, decl);
  }

  private void validateType(Type type, AstNode at, boolean allowVoid) {
    if (type instanceof ClassType ct) {
      if (!globals.getClasses().containsKey(ct.getName())) {
        error(at, "unknown type '" + ct.getName() + "'");
      }
    } else if (type instanceof ReferenceType rt) {
      if (Types.isVoid(rt.getReferencedType())) {
        error(at, "cannot declare a reference to 'void'");
      } else {
        validateType(rt.getReferencedType(), at, false);
      }
    } else if (type instanceof PointerType pt) {
      if (Types.isVoid(pt.getPointeeType())) {
        error(at, "pointers to 'void' are not supported");
      } else {
        validateType(pt.getPointeeType(), at, false);
      }
    } else if (!allowVoid && Types.isVoid(type)) {
      error(at, "'void' is only allowed as a return type");
    }
  }

  private Decl lookupLocal(String name) {
    for (Map<String, Decl> scope : localScopes) {
      Decl decl = scope.get(name);
      if (decl != null) {
        return decl;
      }
    }
    return null;
  }

  private void error(AstNode node, String message) {
    diagnostics.add(Diagnostic.at(Diagnostic.Phase.RESOLVE, node, message));
  }
}
