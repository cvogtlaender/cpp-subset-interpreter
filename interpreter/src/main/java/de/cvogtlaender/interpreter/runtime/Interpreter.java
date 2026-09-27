package de.cvogtlaender.interpreter.runtime;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import de.cvogtlaender.interpreter.semantic.GlobalScope;
import de.cvogtlaender.interpreter.semantic.Types;
import de.cvogtlaender.interpreter.visitor.AstVisitor;

/**
 * Tree-walking interpreter. Expressions evaluate to runtime values (see
 * {@link Cell}); statements evaluate to {@code null}, or to a {@link Returned}
 * signal when a 'return' was executed.
 *
 * Must only run on ASTs that passed the resolver and type checker.
 */
public class Interpreter implements AstVisitor<Object> {

  /** Activation record of a function, method or constructor call. */
  private static final class Frame {
    final Map<Decl, Cell> variables = new IdentityHashMap<>(8);
    // cells of local variables and by-value parameters, in creation order;
    // they die when their scope ends, so that pointers to them dangle
    final List<Cell> owned = new ArrayList<>();
    final ObjectValue self;
    Type returnType;

    Frame(ObjectValue self, Type returnType) {
      this.self = self;
      this.returnType = returnType;
    }
  }

  private record Returned(Object value) {
  }

  private static final Returned VOID_RETURN = new Returned(null);

  /** Maximum nesting of calls, standing in for the size of a native stack. */
  public static final int MAX_CALL_DEPTH = 100_000;

  private final GlobalScope globals;
  private final PrintStream out;
  private final Frame sessionFrame = new Frame(null, null);
  private final Map<ClassDecl, Map<String, MethodDecl>> vtables = new HashMap<>();
  private Frame frame = sessionFrame;
  private int callDepth;

  public Interpreter(GlobalScope globals, PrintStream out) {
    this.globals = globals;
    this.out = out;
  }

  public int run(Program program) {
    FunctionDecl main = findMain();
    Object result = guard(() -> invoke(main.getParameters(), new Cell[0], main.getBody(), null,
        main.getReturnType()));
    out.flush();
    return result instanceof Integer i ? i : 0;
  }

  public Object runMainInSession(FunctionDecl main) {
    Object result = guard(() -> {
      frame = sessionFrame;
      sessionFrame.returnType = main.getReturnType();
      try {
        Returned r = executeStatements(main.getBody().getStatements());
        return r == null ? null : r.value();
      } finally {
        sessionFrame.returnType = null;
      }
    });
    out.flush();
    return result;
  }

  /** REPL: executes a statement in the session frame. */
  public void executeSessionStatement(Stmt stmt) {
    guard(() -> {
      frame = sessionFrame;
      stmt.accept(this);
      return null;
    });
    out.flush();
  }

  /** REPL: evaluates an expression in the session frame. */
  public Object evaluateSessionExpr(Expr expr) {
    Object result = guard(() -> {
      frame = sessionFrame;
      return expr.accept(this);
    });
    out.flush();
    return result;
  }

  public boolean isSessionVariableDefined(Decl decl) {
    return sessionFrame.variables.containsKey(decl);
  }

  /** Current value of a session variable, or null if it was never initialized. */
  public Object sessionValue(Decl decl) {
    Cell cell = sessionFrame.variables.get(decl);
    return cell == null ? null : cell.value;
  }

  private interface Action {
    Object run();
  }

  private Object guard(Action action) {
    Frame saved = frame;
    int savedDepth = callDepth;
    try {
      return action.run();
    } catch (StackOverflowError e) {
      // deeply nested expressions can still exhaust the Java stack
      throw new MiniCppRuntimeException("stack overflow (recursion too deep)", null);
    } finally {
      frame = saved;
      callDepth = savedDepth;
    }
  }

  private FunctionDecl findMain() {
    List<FunctionDecl> mains = globals.getFunctions().get("main");
    if (mains != null) {
      for (FunctionDecl f : mains) {
        if (f.getParameters().isEmpty()) {
          return f;
        }
      }
    }
    throw new MiniCppRuntimeException("no 'main' function defined", null);
  }

  // Calls

  private Object invoke(List<ParameterDecl> params, Cell[] args, BlockStmt body, ObjectValue self,
      Type returnType) {
    Frame callee = new Frame(self, returnType);
    for (int i = 0; i < args.length; i++) {
      callee.variables.put(params.get(i), args[i]);
      if (!Types.isReference(params.get(i).getType())) {
        callee.owned.add(args[i]);
      }
    }
    if (++callDepth > MAX_CALL_DEPTH) {
      callDepth--;
      throw new MiniCppRuntimeException("stack overflow (more than " + MAX_CALL_DEPTH + " nested calls)", body);
    }
    Frame saved = frame;
    frame = callee;
    try {
      Returned r = executeStatements(body.getStatements());
      return r == null ? null : r.value();
    } finally {
      endScope(0);
      frame = saved;
      callDepth--;
    }
  }

  /**
   * Ends the lifetime of the current frame's cells created since {@code mark}.
   */
  private void endScope(int mark) {
    List<Cell> owned = frame.owned;
    if (owned.size() > mark) {
      List<Cell> ending = owned.subList(mark, owned.size());
      ending.forEach(Cell::kill);
      ending.clear();
    }
  }

  private Returned executeStatements(List<Stmt> statements) {
    for (Stmt stmt : statements) {
      Object r = stmt.accept(this);
      if (r != null) {
        return (Returned) r;
      }
    }
    return null;
  }

  private Cell[] bindArguments(List<ParameterDecl> params, List<Expr> args) {
    Cell[] cells = new Cell[args.size()];
    for (int i = 0; i < cells.length; i++) {
      Type paramType = params.get(i).getType();
      Expr arg = args.get(i);
      if (Types.isReference(paramType)) {
        cells[i] = lvalue(arg);
      } else {
        cells[i] = new Cell(storable(arg.accept(this), paramType, arg.isLValue()));
      }
    }
    return cells;
  }

  /**
   * Prepares a value for storing it in a new location of type {@code type}:
   * objects are copied (or sliced), unless they are temporaries already.
   */
  private Object storable(Object value, Type type, boolean fromLValue) {
    if (value instanceof ObjectValue o) {
      ClassDecl target = globals.classOf(type);
      if (target != null && o.getCls() != target) {
        return o.sliceTo(target);
      }
      return fromLValue ? o.copy() : o;
    }
    return value;
  }

  private ObjectValue construct(ClassDecl cls, ConstructorDecl ctor, Cell[] args) {
    ObjectValue obj = new ObjectValue(cls);
    runConstructor(obj, cls, ctor, args);
    obj.setDynamicClass(cls);
    return obj;
  }

  private void runConstructor(ObjectValue obj, ClassDecl cls, ConstructorDecl ctor, Cell[] args) {
    ClassDecl parent = cls.getParent();
    if (parent != null) {
      runConstructor(obj, parent, GlobalScope.defaultConstructor(parent), new Cell[0]);
    }
    obj.setDynamicClass(cls);
    for (FieldDecl f : cls.getFields()) {
      obj.getFields().put(f.getName(), new Cell(defaultValue(f.getType())));
    }
    invoke(ctor.getParameters(), args, ctor.getBody(), obj, Types.VOID);
  }

  private Object defaultValue(Type type) {
    if (type instanceof PrimitiveType p) {
      return switch (p.getKind()) {
        case INT -> 0;
        case BOOL -> false;
        case CHAR -> '\0';
        case STRING -> "";
        case VOID -> null;
      };
    }
    if (Types.isPointer(type)) {
      return Pointer.NULL;
    }
    ClassDecl cls = globals.classOf(type);
    return construct(cls, GlobalScope.defaultConstructor(cls), new Cell[0]);
  }

  private MethodDecl dispatch(MethodDecl method, ObjectValue receiver) {
    if (!method.isEffectivelyVirtual()) {
      return method;
    }
    MethodDecl override = vtable(receiver.getDynamicClass()).get(vtableKey(method));
    return override != null ? override : method;
  }

  private Map<String, MethodDecl> vtable(ClassDecl cls) {
    Map<String, MethodDecl> table = vtables.get(cls);
    if (table == null) {
      table = new LinkedHashMap<>();
      if (cls.getParent() != null) {
        table.putAll(vtable(cls.getParent()));
      }
      for (MethodDecl m : cls.getMethods()) {
        table.put(vtableKey(m), m);
      }
      vtables.put(cls, table);
    }
    return table;
  }

  private static String vtableKey(MethodDecl m) {
    return m.getName() + "(" + Types.parameterKey(m.getParameters()) + ")";
  }

  private void callBuiltin(FunctionDecl f, Object argument) {
    out.print(Values.format(argument));
    out.print('\n');
  }

  // Declarations

  @Override
  public Object visitProgram(Program node) {
    throw new UnsupportedOperationException("use run()");
  }

  @Override
  public Object visitClassDecl(ClassDecl node) {
    return null;
  }

  @Override
  public Object visitConstructorDecl(ConstructorDecl node) {
    return null;
  }

  @Override
  public Object visitFieldDecl(FieldDecl node) {
    return null;
  }

  @Override
  public Object visitFunctionDecl(FunctionDecl node) {
    return null;
  }

  @Override
  public Object visitMethodDecl(MethodDecl node) {
    return null;
  }

  @Override
  public Object visitParameterDecl(ParameterDecl node) {
    return null;
  }

  @Override
  public Object visitVariableDecl(VariableDecl node) {
    Type type = node.getType();
    Expr init = node.getInitializer();
    Cell cell;
    if (Types.isReference(type)) {
      cell = lvalue(init);
    } else {
      cell = new Cell(init != null ? storable(init.accept(this), type, init.isLValue()) : defaultValue(type));
      frame.owned.add(cell);
    }
    frame.variables.put(node, cell);
    return null;
  }

  // Statements

  @Override
  public Object visitBlockStmt(BlockStmt node) {
    int mark = frame.owned.size();
    try {
      return executeStatements(node.getStatements());
    } finally {
      endScope(mark);
    }
  }

  @Override
  public Object visitDeleteStmt(DeleteStmt node) {
    Cell target = ((Pointer) node.getPointer().accept(this)).target();
    if (target == null) {
      return null; // deleting nullptr does nothing
    }
    if (!target.isHeap()) {
      throw new MiniCppRuntimeException("'delete' of a pointer that was not obtained from 'new'", node);
    }
    if (target.isDead()) {
      throw new MiniCppRuntimeException("double delete: the object was already deleted", node);
    }
    target.kill();
    return null;
  }

  @Override
  public Object visitVariableStmt(VariableStmt node) {
    return node.getVariableDecl().accept(this);
  }

  @Override
  public Object visitExprStmt(ExprStmt node) {
    node.getExpression().accept(this);
    return null;
  }

  @Override
  public Object visitIfStmt(IfStmt node) {
    if (truthy(node.getCondition().accept(this))) {
      return node.getIfBranch().accept(this);
    }
    if (node.getElseBranch() != null) {
      return node.getElseBranch().accept(this);
    }
    return null;
  }

  @Override
  public Object visitWhileStmt(WhileStmt node) {
    while (truthy(node.getCondition().accept(this))) {
      Object r = node.getBody().accept(this);
      if (r != null) {
        return r;
      }
    }
    return null;
  }

  private static boolean truthy(Object value) {
    return switch (value) {
      case Boolean b -> b;
      case Integer i -> i != 0;
      case Character c -> c != '\0';
      case Pointer p -> !p.isNull();
      default -> throw new IllegalStateException("not a condition value: " + value);
    };
  }

  @Override
  public Object visitReturnStmt(ReturnStmt node) {
    Expr value = node.getReturnValue();
    if (value == null) {
      return VOID_RETURN;
    }
    Object result = value.accept(this);
    if (Types.isVoid(frame.returnType)) {
      return VOID_RETURN;
    }
    return new Returned(storable(result, frame.returnType, value.isLValue()));
  }

  // Expressions

  @Override
  public Object visitIntLiteral(IntLiteral node) {
    return node.getValue();
  }

  @Override
  public Object visitBoolLiteral(BoolLiteral node) {
    return node.getValue();
  }

  @Override
  public Object visitCharLiteral(CharLiteral node) {
    return node.getValue();
  }

  @Override
  public Object visitStringLiteral(StringLiteral node) {
    return node.getValue();
  }

  @Override
  public Object visitNullptrLiteral(NullptrLiteral node) {
    return Pointer.NULL;
  }

  @Override
  public Object visitNewExpr(NewExpr node) {
    Object value;
    ConstructorDecl ctor = node.getConstructor();
    if (ctor != null) {
      value = construct(ctor.getOwner(), ctor, bindArguments(ctor.getParameters(), node.getArguments()));
    } else if (node.getArguments().isEmpty()) {
      value = defaultValue(node.getAllocatedType());
    } else {
      Expr arg = node.getArguments().get(0);
      value = storable(arg.accept(this), node.getAllocatedType(), arg.isLValue());
    }
    return new Pointer(Cell.heap(value));
  }

  @Override
  public Object visitErrorExpr(ErrorExpr node) {
    throw new IllegalStateException("erroneous expression reached the interpreter: " + node.getMessage());
  }

  @Override
  public Object visitVarExpr(VarExpr node) {
    return lvalue(node).value;
  }

  @Override
  public Object visitMemberAccessExpr(MemberAccessExpr node) {
    return object(node).field(node.getMemberName()).value;
  }

  /** The object whose member 'obj.m' or 'p->m' accesses. */
  private ObjectValue object(MemberAccessExpr m) {
    Object value = m.getObj().accept(this);
    return (ObjectValue) (m.isArrow() ? deref(value, m).value : value);
  }

  /**
   * The cell a pointer points to; fails for null, dangling and deleted pointers.
   */
  private static Cell deref(Object pointer, AstNode at) {
    Cell target = ((Pointer) pointer).target();
    if (target == null) {
      throw new MiniCppRuntimeException("null pointer dereference", at);
    }
    return alive(target, at);
  }

  private static Cell alive(Cell cell, AstNode at) {
    if (cell.isDead()) {
      throw new MiniCppRuntimeException(cell.isHeap() ? "use of deleted memory"
          : "dangling pointer or reference: the variable it refers to no longer exists", at);
    }
    return cell;
  }

  /** The storage location an lvalue expression denotes. */
  private Cell lvalue(Expr expr) {
    switch (expr) {
      case VarExpr v -> {
        if (v.getKind() == VarExpr.Kind.FIELD) {
          return frame.self.field(v.getName());
        }
        Cell cell = frame.variables.get(v.getResolvedDecl());
        if (cell == null) {
          cell = sessionFrame.variables.get(v.getResolvedDecl());
        }
        if (cell == null) {
          throw new MiniCppRuntimeException("variable '" + v.getName() + "' was never initialized", v);
        }
        // a reference may outlive what it refers to (e.g. 'int& r = *p; delete p;')
        return alive(cell, v);
      }
      case MemberAccessExpr m -> {
        return object(m).field(m.getMemberName());
      }
      case UnaryExpr u when u.getOperator() == UnaryExpr.Operator.DEREF -> {
        return deref(u.getExpr().accept(this), u);
      }
      case AssignExpr a -> {
        return assign(a);
      }
      default -> throw new IllegalStateException("not an lvalue: " + expr.toStringTree());
    }
  }

  @Override
  public Object visitAssignExpr(AssignExpr node) {
    return assign(node).value;
  }

  private Cell assign(AssignExpr node) {
    Cell target = lvalue(node.getTarget());
    Object value = node.getValue().accept(this);
    if (value instanceof ObjectValue source) {
      // assign in place, so references into the target object stay valid;
      // only the part belonging to the target's static type is copied (slicing)
      ClassDecl cls = globals.classOf(node.getTarget().getInferredType());
      assignObject((ObjectValue) target.value, source, cls);
    } else {
      target.value = value;
    }
    return target;
  }

  private void assignObject(ObjectValue target, ObjectValue source, ClassDecl cls) {
    if (target == source) {
      return;
    }
    for (FieldDecl f : GlobalScope.allFields(cls)) {
      Cell to = target.field(f.getName());
      Object from = source.field(f.getName()).value;
      if (from instanceof ObjectValue nested) {
        assignObject((ObjectValue) to.value, nested, globals.classOf(f.getType()));
      } else {
        to.value = from;
      }
    }
  }

  @Override
  public Object visitBinaryExpr(BinaryExpr node) {
    BinaryExpr.Operator op = node.getOperator();

    if (op == BinaryExpr.Operator.AND) {
      return (Boolean) node.getLeftHandSide().accept(this) && (Boolean) node.getRightHandSide().accept(this);
    }
    if (op == BinaryExpr.Operator.OR) {
      return (Boolean) node.getLeftHandSide().accept(this) || (Boolean) node.getRightHandSide().accept(this);
    }

    Object left = node.getLeftHandSide().accept(this);
    Object right = node.getRightHandSide().accept(this);

    return switch (op) {
      case ADD -> (Integer) left + (Integer) right;
      case SUB -> (Integer) left - (Integer) right;
      case MUL -> (Integer) left * (Integer) right;
      case DIV -> {
        if ((Integer) right == 0) {
          throw new MiniCppRuntimeException("division by zero", node);
        }
        yield (Integer) left / (Integer) right;
      }
      case MOD -> {
        if ((Integer) right == 0) {
          throw new MiniCppRuntimeException("modulo by zero", node);
        }
        yield (Integer) left % (Integer) right;
      }
      case EQ -> Objects.equals(left, right);
      case NEQ -> !Objects.equals(left, right);
      case LT -> compare(left, right) < 0;
      case LE -> compare(left, right) <= 0;
      case GT -> compare(left, right) > 0;
      case GE -> compare(left, right) >= 0;
      default -> throw new IllegalStateException();
    };
  }

  private static int compare(Object left, Object right) {
    if (left instanceof Integer l) {
      return Integer.compare(l, (Integer) right);
    }
    return Character.compare((Character) left, (Character) right);
  }

  @Override
  public Object visitUnaryExpr(UnaryExpr node) {
    if (node.getOperator() == UnaryExpr.Operator.ADDRESS_OF) {
      return new Pointer(lvalue(node.getExpr()));
    }
    Object value = node.getExpr().accept(this);
    return switch (node.getOperator()) {
      case NOT -> !(Boolean) value;
      case NEGATE -> -(Integer) value;
      case POSITIVE -> value;
      case DEREF -> deref(value, node).value;
      case ADDRESS_OF -> throw new IllegalStateException();
    };
  }

  @Override
  public Object visitCallExpr(CallExpr node) {
    switch (node.getKind()) {
      case FUNCTION -> {
        FunctionDecl f = (FunctionDecl) node.getTarget();
        if (f.isBuiltin()) {
          callBuiltin(f, node.getArguments().get(0).accept(this));
          return null;
        }
        Cell[] args = bindArguments(f.getParameters(), node.getArguments());
        return invoke(f.getParameters(), args, f.getBody(), null, f.getReturnType());
      }
      case METHOD -> {
        ObjectValue receiver;
        if (node.getCallee() instanceof MemberAccessExpr m) {
          receiver = object(m);
        } else {
          receiver = frame.self;
        }
        MethodDecl method = dispatch((MethodDecl) node.getTarget(), receiver);
        Cell[] args = bindArguments(method.getParameters(), node.getArguments());
        return invoke(method.getParameters(), args, method.getBody(), receiver, method.getReturnType());
      }
      case CONSTRUCTOR -> {
        ConstructorDecl ctor = (ConstructorDecl) node.getTarget();
        Cell[] args = bindArguments(ctor.getParameters(), node.getArguments());
        return construct(ctor.getOwner(), ctor, args);
      }
      default -> throw new IllegalStateException();
    }
  }

  // Types (not evaluated)

  @Override
  public Object visitClassType(ClassType node) {
    return null;
  }

  @Override
  public Object visitPointerType(PointerType node) {
    return null;
  }

  @Override
  public Object visitNullptrType(NullptrType node) {
    return null;
  }

  @Override
  public Object visitPrimitiveType(PrimitiveType node) {
    return null;
  }

  @Override
  public Object visitReferenceType(ReferenceType node) {
    return null;
  }
}
