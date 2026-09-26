package de.cvogtlaender.interpreter.visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.cvogtlaender.interpreter.ast.AstNode;
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

/**
 * Declaration-based type checker. Runs after {@link ASTResolveVisitor} and
 * annotates every expression with its (non-reference) type and lvalue-ness,
 * picks overloads for calls, and computes which methods are virtual.
 *
 * Visiting an expression returns its type, or {@code null} if it is erroneous
 * (errors are reported once, at the innermost node).
 */
public class TypeCheckVisitor implements AstVisitor<Type> {

  private final GlobalScope globals;
  private final List<Diagnostic> diagnostics = new ArrayList<>();
  private final Set<ClassDecl> checkedClasses = new HashSet<>();

  // context of the callable currently being checked; null at the REPL prompt
  private Type currentReturnType;
  private String currentCallableName;
  private boolean inConstructor;

  public TypeCheckVisitor(GlobalScope globals) {
    this.globals = globals;
  }

  public List<Diagnostic> getDiagnostics() {
    return diagnostics;
  }

  public boolean hasErrors() {
    return !diagnostics.isEmpty();
  }

  // Entry points

  public void check(Program program) {
    for (ClassDecl c : program.getClassDefs()) {
      checkClassDeclaration(c);
    }
    visitProgram(program);
  }

  public void checkEntryPoint(Program program) {
    List<FunctionDecl> mains = program.getFunctions().stream().filter(f -> f.getName().equals("main")).toList();
    if (mains.isEmpty()) {
      diagnostics.add(new Diagnostic(Diagnostic.Phase.TYPE, 0, 0, 0, 0, "no 'main' function defined"));
    }
    for (FunctionDecl main : mains) {
      validateMain(main);
    }
  }

  private void validateMain(FunctionDecl main) {
    if (!main.getParameters().isEmpty()) {
      error(main, "'main' must not take parameters");
    }
    Type ret = main.getReturnType();
    if (!Types.is(ret, PrimitiveType.Kind.INT) && !Types.isVoid(ret)) {
      error(main, "'main' must return 'int' or 'void'");
    }
  }

  /** REPL: checks a newly declared class. */
  public void checkClass(ClassDecl c) {
    checkClassDeclaration(c);
    c.accept(this);
  }

  /** REPL: checks a newly declared function. */
  public void checkFunction(FunctionDecl f) {
    if (f.getName().equals("main")) {
      validateMain(f);
    }
    f.accept(this);
  }

  /** REPL: checks a statement entered at the prompt. */
  public void checkSessionStatement(Stmt stmt) {
    currentReturnType = null;
    currentCallableName = null;
    inConstructor = false;
    stmt.accept(this);
  }

  /** REPL: checks a bare expression entered at the prompt, returns its type. */
  public Type checkSessionExpr(Expr expr) {
    currentReturnType = null;
    return expr.accept(this);
  }

  // Classes

  private void checkClassDeclaration(ClassDecl c) {
    if (!checkedClasses.add(c)) {
      return;
    }
    ClassDecl parent = c.getParent();
    if (parent != null) {
      checkClassDeclaration(parent);
      if (GlobalScope.defaultConstructor(parent) == null) {
        error(c, "base class '" + parent.getClassName() + "' of '" + c.getClassName()
            + "' has no parameterless constructor");
      }
    }

    for (MethodDecl m : c.getMethods()) {
      MethodDecl overridden = findOverridden(parent, m);
      if (overridden == null) {
        m.setEffectivelyVirtual(m.getIsVirtual());
        continue;
      }
      if (!Types.same(overridden.getReturnType(), m.getReturnType())) {
        error(m, "return type '" + m.getReturnType().getName() + "' of '" + m.getName()
            + "' differs from the overridden method in '" + overridden.getOwner().getClassName() + "' ('"
            + overridden.getReturnType().getName() + "')");
      }
      m.setEffectivelyVirtual(m.getIsVirtual() || overridden.isEffectivelyVirtual());
    }

    for (FieldDecl f : c.getFields()) {
      ClassDecl fieldClass = globals.classOf(f.getType());
      if (fieldClass != null && GlobalScope.defaultConstructor(fieldClass) == null) {
        error(f, "field '" + f.getName() + "' of type '" + fieldClass.getClassName()
            + "' requires a parameterless constructor");
      }
    }

    if (containsItself(c, c, new HashSet<>())) {
      error(c, "class '" + c.getClassName() + "' contains itself (it would have infinite size)");
    }
  }

  private static MethodDecl findOverridden(ClassDecl start, MethodDecl m) {
    String key = Types.parameterKey(m.getParameters());
    for (ClassDecl c = start; c != null; c = c.getParent()) {
      for (MethodDecl candidate : c.getMethods()) {
        if (candidate.getName().equals(m.getName()) && Types.parameterKey(candidate.getParameters()).equals(key)) {
          return candidate;
        }
      }
    }
    return null;
  }

  private boolean containsItself(ClassDecl root, ClassDecl current, Set<ClassDecl> visited) {
    if (!visited.add(current)) {
      return false;
    }
    List<ClassDecl> parts = new ArrayList<>();
    if (current.getParent() != null) {
      parts.add(current.getParent());
    }
    for (FieldDecl f : current.getFields()) {
      ClassDecl fieldClass = globals.classOf(f.getType());
      if (fieldClass != null) {
        parts.add(fieldClass);
      }
    }
    for (ClassDecl part : parts) {
      if (part == root || containsItself(root, part, visited)) {
        return true;
      }
    }
    return false;
  }

  // Declarations

  @Override
  public Type visitProgram(Program node) {
    for (ClassDecl c : node.getClassDefs()) {
      c.accept(this);
    }
    for (FunctionDecl f : node.getFunctions()) {
      f.accept(this);
    }
    return null;
  }

  @Override
  public Type visitClassDecl(ClassDecl node) {
    for (ConstructorDecl c : node.getConstructors()) {
      c.accept(this);
    }
    for (MethodDecl m : node.getMethods()) {
      m.accept(this);
    }
    return null;
  }

  @Override
  public Type visitFunctionDecl(FunctionDecl node) {
    if (node.isBuiltin()) {
      return null;
    }
    checkBody(node.getName(), node.getReturnType(), node.getBody(), false, node,
        node.getName().equals("main"));
    return null;
  }

  @Override
  public Type visitMethodDecl(MethodDecl node) {
    checkBody(node.getOwner().getClassName() + "::" + node.getName(), node.getReturnType(), node.getBody(), false,
        node, false);
    return null;
  }

  @Override
  public Type visitConstructorDecl(ConstructorDecl node) {
    checkBody(node.getName(), Types.VOID, node.getBody(), true, node, false);
    return null;
  }

  private void checkBody(String name, Type returnType, BlockStmt body, boolean constructor, AstNode at,
      boolean isMain) {
    Type savedReturn = currentReturnType;
    String savedName = currentCallableName;
    boolean savedCtor = inConstructor;
    currentReturnType = returnType;
    currentCallableName = name;
    inConstructor = constructor;
    try {
      body.accept(this);
      if (!Types.isVoid(returnType) && !isMain && !alwaysReturns(body)) {
        error(at, "non-void function '" + name + "' does not return a value on all paths");
      }
    } finally {
      currentReturnType = savedReturn;
      currentCallableName = savedName;
      inConstructor = savedCtor;
    }
  }

  public static boolean alwaysReturns(Stmt stmt) {
    return switch (stmt) {
      case ReturnStmt r -> true;
      case BlockStmt b -> b.getStatements().stream().anyMatch(TypeCheckVisitor::alwaysReturns);
      case IfStmt i -> i.getElseBranch() != null && alwaysReturns(i.getIfBranch()) && alwaysReturns(i.getElseBranch());
      // 'while (true)' never completes normally
      case WhileStmt w -> w.getCondition() instanceof BoolLiteral b && b.getValue();
      default -> false;
    };
  }

  @Override
  public Type visitFieldDecl(FieldDecl node) {
    return null;
  }

  @Override
  public Type visitParameterDecl(ParameterDecl node) {
    return null;
  }

  @Override
  public Type visitVariableDecl(VariableDecl node) {
    Type declared = node.getType();
    Expr init = node.getInitializer();
    Type initType = init == null ? null : init.accept(this);

    if (Types.isReference(declared)) {
      Type target = Types.strip(declared);
      if (init == null) {
        error(node, "reference '" + node.getName() + "' must be initialized");
      } else if (initType != null) {
        if (!init.isLValue()) {
          error(init, "cannot bind reference '" + node.getName() + "' to a temporary value");
        } else if (!bindable(target, initType)) {
          error(init, "cannot bind '" + declared.getName() + "' to a value of type '" + initType.getName() + "'");
        }
      }
      return null;
    }

    if (init != null) {
      if (initType != null && !assignable(declared, initType)) {
        error(init, "cannot initialize '" + node.getName() + "' of type '" + declared.getName()
            + "' with a value of type '" + initType.getName() + "'");
      }
    } else {
      ClassDecl cls = globals.classOf(declared);
      if (cls != null && GlobalScope.defaultConstructor(cls) == null) {
        error(node, "class '" + cls.getClassName() + "' has no parameterless constructor");
      }
    }
    return null;
  }

  // Statements

  @Override
  public Type visitBlockStmt(BlockStmt node) {
    for (Stmt stmt : node.getStatements()) {
      stmt.accept(this);
    }
    return null;
  }

  @Override
  public Type visitVariableStmt(VariableStmt node) {
    return node.getVariableDecl().accept(this);
  }

  @Override
  public Type visitExprStmt(ExprStmt node) {
    node.getExpression().accept(this);
    return null;
  }

  @Override
  public Type visitDeleteStmt(DeleteStmt node) {
    Type type = node.getPointer().accept(this);
    if (type != null && !Types.isPointer(type)) {
      error(node.getPointer(), "cannot delete a value of non-pointer type '" + type.getName() + "'");
    }
    return null;
  }

  @Override
  public Type visitIfStmt(IfStmt node) {
    checkCondition(node.getCondition(), "if");
    node.getIfBranch().accept(this);
    if (node.getElseBranch() != null) {
      node.getElseBranch().accept(this);
    }
    return null;
  }

  @Override
  public Type visitWhileStmt(WhileStmt node) {
    checkCondition(node.getCondition(), "while");
    node.getBody().accept(this);
    return null;
  }

  // the only place with an implicit conversion to bool
  private void checkCondition(Expr condition, String statement) {
    Type type = condition.accept(this);
    if (type != null && !Types.is(type, PrimitiveType.Kind.BOOL) && !Types.is(type, PrimitiveType.Kind.INT)
        && !Types.is(type, PrimitiveType.Kind.CHAR) && !Types.isPointerLike(type)) {
      error(condition, "'" + statement + "' condition of type '" + type.getName() + "' cannot be converted to bool");
    }
  }

  @Override
  public Type visitReturnStmt(ReturnStmt node) {
    Expr value = node.getReturnValue();
    Type valueType = value == null ? null : value.accept(this);

    if (currentReturnType == null) {
      error(node, "'return' outside of a function");
      return null;
    }
    if (inConstructor) {
      if (value != null) {
        error(value, "constructor '" + currentCallableName + "' cannot return a value");
      }
      return null;
    }
    if (Types.isVoid(currentReturnType)) {
      if (value != null && valueType != null && !Types.isVoid(valueType)) {
        error(value, "void function '" + currentCallableName + "' cannot return a value");
      }
      return null;
    }
    if (value == null) {
      error(node, "non-void function '" + currentCallableName + "' must return a value of type '"
          + currentReturnType.getName() + "'");
    } else if (valueType != null && !assignable(currentReturnType, valueType)) {
      error(value, "cannot return a value of type '" + valueType.getName() + "' from '" + currentCallableName
          + "' returning '" + currentReturnType.getName() + "'");
    }
    return null;
  }

  // Expressions

  @Override
  public Type visitIntLiteral(IntLiteral node) {
    return typed(node, Types.INT, false);
  }

  @Override
  public Type visitBoolLiteral(BoolLiteral node) {
    return typed(node, Types.BOOL, false);
  }

  @Override
  public Type visitCharLiteral(CharLiteral node) {
    return typed(node, Types.CHAR, false);
  }

  @Override
  public Type visitStringLiteral(StringLiteral node) {
    return typed(node, Types.STRING, false);
  }

  @Override
  public Type visitNullptrLiteral(NullptrLiteral node) {
    return typed(node, Types.NULLPTR, false);
  }

  @Override
  public Type visitErrorExpr(ErrorExpr node) {
    error(node, node.getMessage());
    return null;
  }

  @Override
  public Type visitVarExpr(VarExpr node) {
    if (node.getKind() == null) {
      return null; // reported by the resolver
    }
    return switch (node.getKind()) {
      case VARIABLE -> typed(node, Types.strip(declaredType(node.getResolvedDecl())), true);
      case FIELD -> typed(node, ((FieldDecl) node.getResolvedDecl()).getType(), true);
      case METHOD, FUNCTION -> {
        error(node, "'" + node.getName() + "' is a function; it must be called");
        yield null;
      }
      case CLASS -> {
        error(node, "'" + node.getName() + "' is a class name, not a value");
        yield null;
      }
    };
  }

  private static Type declaredType(Decl decl) {
    return switch (decl) {
      case VariableDecl v -> v.getType();
      case ParameterDecl p -> p.getType();
      default -> throw new IllegalStateException("not a variable: " + decl);
    };
  }

  @Override
  public Type visitMemberAccessExpr(MemberAccessExpr node) {
    Type objType = node.getObj().accept(this);
    if (objType == null) {
      return null;
    }
    ClassDecl cls = accessedClass(node, objType);
    if (cls == null) {
      return null;
    }
    ClassDecl owner = GlobalScope.findMemberOwner(cls, node.getMemberName());
    if (owner == null) {
      error(node, "class '" + cls.getClassName() + "' has no member named '" + node.getMemberName() + "'");
      return null;
    }
    FieldDecl field = GlobalScope.findField(owner, node.getMemberName());
    if (field == null || field.getOwner() != owner) {
      error(node, "method '" + node.getMemberName() + "' must be called");
      return null;
    }
    node.setResolvedField(field);
    return typed(node, field.getType(), node.isArrow() || node.getObj().isLValue());
  }

  /**
   * The class whose member {@code m} accesses: the class of the object for
   * 'obj.m', the pointee class for 'p->m'. Reports an error and returns null
   * if there is none.
   */
  private ClassDecl accessedClass(MemberAccessExpr m, Type objType) {
    if (m.isArrow()) {
      ClassDecl cls = objType instanceof PointerType p ? globals.classOf(p.getPointeeType()) : null;
      if (cls == null) {
        error(m, "member access '->" + m.getMemberName() + "' on a value of type '" + objType.getName()
            + "', which is not a pointer to a class");
      }
      return cls;
    }
    ClassDecl cls = globals.classOf(objType);
    if (cls == null) {
      boolean classPointer = objType instanceof PointerType p && globals.classOf(p.getPointeeType()) != null;
      error(m, "member access '." + m.getMemberName() + "' on a value of non-class type '" + objType.getName()
          + "'" + (classPointer ? "; did you mean to use '->'?" : ""));
    }
    return cls;
  }

  @Override
  public Type visitAssignExpr(AssignExpr node) {
    Type targetType = node.getTarget().accept(this);
    Type valueType = node.getValue().accept(this);
    if (targetType == null || valueType == null) {
      return null;
    }
    if (!node.getTarget().isLValue()) {
      error(node.getTarget(), "expression is not assignable");
      return null;
    }
    if (!assignable(targetType, valueType)) {
      error(node, "cannot assign a value of type '" + valueType.getName() + "' to '" + targetType.getName() + "'");
      return null;
    }
    return typed(node, targetType, true);
  }

  @Override
  public Type visitBinaryExpr(BinaryExpr node) {
    Type left = node.getLeftHandSide().accept(this);
    Type right = node.getRightHandSide().accept(this);
    if (left == null || right == null) {
      return null;
    }

    boolean ok;
    Type result;
    switch (node.getOperator()) {
      case ADD, SUB, MUL, DIV, MOD -> {
        ok = Types.is(left, PrimitiveType.Kind.INT) && Types.is(right, PrimitiveType.Kind.INT);
        result = Types.INT;
      }
      case LT, LE, GT, GE -> {
        ok = Types.same(left, right)
            && (Types.is(left, PrimitiveType.Kind.INT) || Types.is(left, PrimitiveType.Kind.CHAR));
        result = Types.BOOL;
      }
      case EQ, NEQ -> {
        ok = Types.same(left, right) && left instanceof PrimitiveType && !Types.isVoid(left)
            || Types.isPointerLike(left) && Types.isPointerLike(right)
                && (assignable(left, right) || assignable(right, left));
        result = Types.BOOL;
      }
      case AND, OR -> {
        ok = Types.is(left, PrimitiveType.Kind.BOOL) && Types.is(right, PrimitiveType.Kind.BOOL);
        result = Types.BOOL;
      }
      default -> throw new IllegalStateException();
    }

    if (!ok) {
      error(node, "invalid operands to binary '" + symbol(node.getOperator()) + "' ('" + left.getName()
          + "' and '" + right.getName() + "')");
      return null;
    }
    return typed(node, result, false);
  }

  public static String symbol(BinaryExpr.Operator op) {
    return switch (op) {
      case ADD -> "+";
      case SUB -> "-";
      case MUL -> "*";
      case DIV -> "/";
      case MOD -> "%";
      case EQ -> "==";
      case NEQ -> "!=";
      case LT -> "<";
      case LE -> "<=";
      case GT -> ">";
      case GE -> ">=";
      case AND -> "&&";
      case OR -> "||";
    };
  }

  @Override
  public Type visitUnaryExpr(UnaryExpr node) {
    Type operand = node.getExpr().accept(this);
    if (operand == null) {
      return null;
    }
    if (node.getOperator() == UnaryExpr.Operator.DEREF) {
      if (!(operand instanceof PointerType p)) {
        error(node, "invalid operand to unary '*' ('" + operand.getName() + "'); only pointers can be dereferenced");
        return null;
      }
      return typed(node, p.getPointeeType(), true);
    }
    if (node.getOperator() == UnaryExpr.Operator.ADDRESS_OF) {
      if (!node.getExpr().isLValue()) {
        error(node, "cannot take the address of a temporary value");
        return null;
      }
      return typed(node, new PointerType(operand), false);
    }
    if (node.getOperator() == UnaryExpr.Operator.NOT) {
      if (!Types.is(operand, PrimitiveType.Kind.BOOL)) {
        error(node, "invalid operand to unary '!' ('" + operand.getName() + "')");
        return null;
      }
      return typed(node, Types.BOOL, false);
    }
    if (!Types.is(operand, PrimitiveType.Kind.INT)) {
      String op = node.getOperator() == UnaryExpr.Operator.NEGATE ? "-" : "+";
      error(node, "invalid operand to unary '" + op + "' ('" + operand.getName() + "')");
      return null;
    }
    return typed(node, Types.INT, false);
  }

  @Override
  public Type visitCallExpr(CallExpr node) {
    List<Type> argTypes = new ArrayList<>();
    boolean argsOk = true;
    for (Expr arg : node.getArguments()) {
      Type t = arg.accept(this);
      argTypes.add(t);
      argsOk &= t != null;
    }

    Expr callee = node.getCallee();

    if (callee instanceof VarExpr v) {
      if (v.getKind() == null) {
        return null; // reported by the resolver
      }
      switch (v.getKind()) {
        case FUNCTION -> {
          if (!argsOk) {
            return null;
          }
          List<FunctionDecl> candidates = globals.getFunctions().get(v.getName());
          FunctionDecl f = pickOverload(v.getName(), candidates, FunctionDecl::getParameters, node, node.getArguments(), argTypes);
          if (f == null) {
            return null;
          }
          node.setKind(CallExpr.Kind.FUNCTION);
          node.setTarget(f);
          return typed(node, f.getReturnType(), false);
        }
        case METHOD -> {
          if (!argsOk) {
            return null;
          }
          List<MethodDecl> candidates = GlobalScope.methodsNamed(v.getMemberOwner(), v.getName());
          MethodDecl m = pickOverload(v.getName(), candidates, MethodDecl::getParameters, node,
              node.getArguments(), argTypes);
          if (m == null) {
            return null;
          }
          node.setKind(CallExpr.Kind.METHOD);
          node.setTarget(m);
          return typed(node, m.getReturnType(), false);
        }
        case CLASS -> {
          if (!argsOk) {
            return null;
          }
          ClassDecl cls = (ClassDecl) v.getResolvedDecl();
          ConstructorDecl ctor = pickOverload(cls.getClassName(), cls.getConstructors(),
              ConstructorDecl::getParameters, node, node.getArguments(), argTypes);
          if (ctor == null) {
            return null;
          }
          node.setKind(CallExpr.Kind.CONSTRUCTOR);
          node.setTarget(ctor);
          return typed(node, new ClassType(cls.getClassName()), false);
        }
        default -> {
          error(callee, "'" + v.getName() + "' is not a function");
          return null;
        }
      }
    }

    if (callee instanceof MemberAccessExpr m) {
      Type objType = m.getObj().accept(this);
      if (objType == null || !argsOk) {
        return null;
      }
      ClassDecl cls = accessedClass(m, objType);
      if (cls == null) {
        return null;
      }
      ClassDecl owner = GlobalScope.findMemberOwner(cls, m.getMemberName());
      if (owner == null) {
        error(m, "class '" + cls.getClassName() + "' has no member named '" + m.getMemberName() + "'");
        return null;
      }
      List<MethodDecl> candidates = GlobalScope.methodsNamed(owner, m.getMemberName());
      if (candidates.isEmpty()) {
        error(m, "'" + m.getMemberName() + "' is a field of '" + owner.getClassName() + "', not a method");
        return null;
      }
      MethodDecl method = pickOverload(m.getMemberName(), candidates, MethodDecl::getParameters, node,
          node.getArguments(), argTypes);
      if (method == null) {
        return null;
      }
      node.setKind(CallExpr.Kind.METHOD);
      node.setTarget(method);
      return typed(node, method.getReturnType(), false);
    }

    callee.accept(this);
    error(callee, "expression is not callable");
    return null;
  }

  @Override
  public Type visitNewExpr(NewExpr node) {
    List<Type> argTypes = new ArrayList<>();
    boolean argsOk = true;
    for (Expr arg : node.getArguments()) {
      Type t = arg.accept(this);
      argTypes.add(t);
      argsOk &= t != null;
    }

    Type type = node.getAllocatedType();
    if (Types.isVoid(type)) {
      return null; // reported by the resolver
    }
    if (Types.isClass(type)) {
      ClassDecl cls = globals.classOf(type);
      if (cls == null || !argsOk) {
        return null;
      }
      ConstructorDecl ctor = pickOverload(cls.getClassName(), cls.getConstructors(),
          ConstructorDecl::getParameters, node, node.getArguments(), argTypes);
      if (ctor == null) {
        return null;
      }
      node.setConstructor(ctor);
    } else if (node.getArguments().size() > 1) {
      error(node, "too many initializers for 'new " + type.getName() + "'");
      return null;
    } else if (!argsOk) {
      return null;
    } else if (!argTypes.isEmpty() && !assignable(type, argTypes.get(0))) {
      error(node.getArguments().get(0), "cannot initialize a new '" + type.getName() + "' with a value of type '"
          + argTypes.get(0).getName() + "'");
      return null;
    }
    return typed(node, new PointerType(type), false);
  }

  /**
   * Overload resolution by exact match of parameter types (including '&').
   * Only if no candidate matches exactly, implicit conversions (derived to base
   * class, derived to base pointer, nullptr to pointer) are considered. More
   * than one best candidate is an error.
   */
  private <D extends Decl> D pickOverload(String name, List<D> candidates,
      Function<D, List<ParameterDecl>> params, AstNode call, List<Expr> args, List<Type> argTypes) {
    List<D> exact = new ArrayList<>();
    List<D> converting = new ArrayList<>();
    for (D candidate : candidates) {
      int cost = matchCost(params.apply(candidate), args, argTypes);
      if (cost == 0) {
        exact.add(candidate);
      } else if (cost > 0) {
        converting.add(candidate);
      }
    }

    List<D> best = !exact.isEmpty() ? exact : converting;
    String callText = name + "(" + argTypes.stream().map(Type::getName).collect(Collectors.joining(", ")) + ")";
    if (best.size() == 1) {
      return best.get(0);
    }

    String options = candidates.stream().map(c -> Types.signature(name, params.apply(c)))
        .collect(Collectors.joining(", "));
    if (best.isEmpty()) {
      error(call, "no matching function for call to '" + callText + "'; candidates are: " + options);
    } else {
      error(call, "call to '" + callText + "' is ambiguous; candidates are: " + best.stream()
          .map(c -> Types.signature(name, params.apply(c))).collect(Collectors.joining(", ")));
    }
    return null;
  }

  /** -1: not viable, 0: exact match, >0: number of implicit conversions. */
  private int matchCost(List<ParameterDecl> params, List<Expr> args, List<Type> argTypes) {
    if (params.size() != args.size()) {
      return -1;
    }
    int cost = 0;
    for (int i = 0; i < params.size(); i++) {
      Type paramType = params.get(i).getType();
      Type argType = argTypes.get(i);
      if (Types.isReference(paramType) && !args.get(i).isLValue()) {
        return -1;
      }
      Type target = Types.strip(paramType);
      if (Types.same(target, argType)) {
        continue;
      }
      // a reference cannot bind to a converted pointer, which would be a temporary
      boolean converts = Types.isReference(paramType) ? bindable(target, argType) : assignable(target, argType);
      if (converts) {
        cost++;
        continue;
      }
      return -1;
    }
    return cost;
  }

  /** Value of type {@code value} may be stored into {@code target} (maybe by slicing). */
  private boolean assignable(Type target, Type value) {
    return bindable(target, value) || pointerConvertible(target, value);
  }

  /** A reference to {@code target} may refer to an lvalue of type {@code value}. */
  private boolean bindable(Type target, Type value) {
    return Types.same(target, value) || isDerivedFrom(value, target);
  }

  /** 'nullptr' to any pointer, 'D*' to 'B*' if D derives from B. */
  private boolean pointerConvertible(Type target, Type value) {
    if (!(target instanceof PointerType t)) {
      return false;
    }
    return value instanceof NullptrType
        || value instanceof PointerType v && isDerivedFrom(v.getPointeeType(), t.getPointeeType());
  }

  private boolean isDerivedFrom(Type derived, Type base) {
    ClassDecl d = globals.classOf(derived);
    ClassDecl b = globals.classOf(base);
    return d != null && b != null && d != b && d.isSubclassOf(b);
  }

  // Types (not visited)

  @Override
  public Type visitClassType(ClassType node) {
    return node;
  }

  @Override
  public Type visitPointerType(PointerType node) {
    return node;
  }

  @Override
  public Type visitNullptrType(NullptrType node) {
    return node;
  }

  @Override
  public Type visitPrimitiveType(PrimitiveType node) {
    return node;
  }

  @Override
  public Type visitReferenceType(ReferenceType node) {
    return node;
  }

  // Helpers

  private static Type typed(Expr expr, Type type, boolean lvalue) {
    expr.setInferredType(type);
    expr.setIsLValue(lvalue);
    return type;
  }

  private void error(AstNode node, String message) {
    diagnostics.add(Diagnostic.at(Diagnostic.Phase.TYPE, node, message));
  }
}
