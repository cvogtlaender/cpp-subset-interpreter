package de.cvogtlaender.interpreter;

import java.util.ArrayList;
import java.util.List;

import de.cvogtlaender.interpreter.ast.ClassDef;
import de.cvogtlaender.interpreter.ast.Constructor;
import de.cvogtlaender.interpreter.ast.FunctionDef;
import de.cvogtlaender.interpreter.ast.MethodDef;
import de.cvogtlaender.interpreter.ast.Parameter;
import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.ast.expression.AssignExpr;
import de.cvogtlaender.interpreter.ast.expression.Expr;
import de.cvogtlaender.interpreter.ast.statement.BlockStmt;
import de.cvogtlaender.interpreter.ast.statement.ExprStmt;
import de.cvogtlaender.interpreter.ast.statement.IfStmt;
import de.cvogtlaender.interpreter.ast.statement.ReturnStmt;
import de.cvogtlaender.interpreter.ast.statement.Stmt;
import de.cvogtlaender.interpreter.ast.statement.VariableStmt;
import de.cvogtlaender.interpreter.ast.statement.WhileStmt;
import de.cvogtlaender.interpreter.ast.type.ClassType;
import de.cvogtlaender.interpreter.ast.type.PrimitiveType;
import de.cvogtlaender.interpreter.ast.type.ReferenceType;
import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.MiniCppBaseVisitor;
import de.cvogtlaender.interpreter.MiniCppParser.AdditiveExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.ArgListContext;
import de.cvogtlaender.interpreter.MiniCppParser.AssignmentExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.BaseTypeContext;
import de.cvogtlaender.interpreter.MiniCppParser.BlockContext;
import de.cvogtlaender.interpreter.MiniCppParser.ClassDefContext;
import de.cvogtlaender.interpreter.MiniCppParser.DeclarationContext;
import de.cvogtlaender.interpreter.MiniCppParser.EqualityExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.ExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.FunctionDefContext;
import de.cvogtlaender.interpreter.MiniCppParser.IfStmtContext;
import de.cvogtlaender.interpreter.MiniCppParser.LiteralContext;
import de.cvogtlaender.interpreter.MiniCppParser.LogicalAndExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.LogicalOrExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.MultiplicativeExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.ParamContext;
import de.cvogtlaender.interpreter.MiniCppParser.ParamListContext;
import de.cvogtlaender.interpreter.MiniCppParser.PostfixExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.PostfixPartContext;
import de.cvogtlaender.interpreter.MiniCppParser.PrimaryExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.ProgramContext;
import de.cvogtlaender.interpreter.MiniCppParser.RelationalExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.ReturnStmtContext;
import de.cvogtlaender.interpreter.MiniCppParser.StatementContext;
import de.cvogtlaender.interpreter.MiniCppParser.TypeContext;
import de.cvogtlaender.interpreter.MiniCppParser.TypeRefContext;
import de.cvogtlaender.interpreter.MiniCppParser.UnaryExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.VarDeclContext;
import de.cvogtlaender.interpreter.MiniCppParser.WhileStmtContext;

public class ASTVisitor extends MiniCppBaseVisitor<Object> {

  @Override
  public Object visitAdditiveExpr(AdditiveExprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAdditiveExpr(ctx);
  }

  @Override
  public Object visitArgList(ArgListContext ctx) {
    // TODO Auto-generated method stub
    return super.visitArgList(ctx);
  }

  @Override
  public Object visitAssignmentExpr(AssignmentExprContext ctx) {

    if (ctx.assignmentExpr() == null)
      return visitLogicalOrExpr(ctx.logicalOrExpr());

    Expr target = (Expr) visitLogicalOrExpr(ctx.logicalOrExpr());
    Expr value = (Expr) visitAssignmentExpr(ctx.assignmentExpr());

    AssignExpr assignExpr = new AssignExpr(target, value);

    return assignExpr;
  }

  @Override
  public Object visitBaseType(BaseTypeContext ctx) {
    PrimitiveType.Kind kind = null;

    if (ctx.INT() != null) {
      kind = PrimitiveType.Kind.INT;
    } else if (ctx.BOOL() != null) {
      kind = PrimitiveType.Kind.BOOL;
    } else if (ctx.CHAR() != null) {
      kind = PrimitiveType.Kind.CHAR;
    } else if (ctx.STRING() != null) {
      kind = PrimitiveType.Kind.STRING;
    } else {
      kind = PrimitiveType.Kind.VOID;
    }

    PrimitiveType primitiveType = new PrimitiveType(kind);

    return primitiveType;
  }

  @Override
  public Object visitBlock(BlockContext ctx) {
    List<Stmt> statements = new ArrayList<>();

    for (StatementContext statement : ctx.statement()) {
      statements.add((Stmt) visitStatement(statement));
    }

    BlockStmt blockStmt = new BlockStmt(statements);

    return blockStmt;
  }

  @Override
  public Object visitClassDef(ClassDefContext ctx) {

    List<Parameter> fields = new ArrayList<>();
    fields.add(new Parameter(new PrimitiveType(PrimitiveType.Kind.INT), "count"));

    List<Constructor> constructors = new ArrayList<>();
    constructors.add(new Constructor("Sub", fields, new BlockStmt(new ArrayList<>())));

    List<MethodDef> methods = new ArrayList<>();
    methods.add(new MethodDef(new PrimitiveType(PrimitiveType.Kind.INT), "getCount", new ArrayList<>(),
        new BlockStmt(new ArrayList<>()), false));

    ClassDef classDef = new ClassDef("Sub", "Super", fields, constructors, methods);

    return classDef;
  }

  @Override
  public Object visitDeclaration(DeclarationContext ctx) {
    if (ctx.functionDef() != null)
      return visitFunctionDef(ctx.functionDef());

    return visitClassDef(ctx.classDef());
  }

  @Override
  public Object visitEqualityExpr(EqualityExprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitEqualityExpr(ctx);
  }

  @Override
  public Object visitExpr(ExprContext ctx) {
    // return visitAssignmentExpr(ctx.assignmentExpr());
    return null;
  }

  @Override
  public Object visitFunctionDef(FunctionDefContext ctx) {
    Type returnType = (Type) visitType(ctx.type());
    String name = ctx.Identifier().getText();
    List<Parameter> parameters = (List<Parameter>) visitParamList(ctx.paramList());
    BlockStmt body = (BlockStmt) visitBlock(ctx.block());

    FunctionDef function = new FunctionDef(returnType, name, parameters, body);

    return function;
  }

  @Override
  public Object visitIfStmt(IfStmtContext ctx) {
    Expr condition = (Expr) visitExpr(ctx.expr());
    Stmt ifBranch = (Stmt) visitStatement(ctx.statement(0));
    Stmt elseBranch = null;

    if (ctx.statement().size() > 1) {
      elseBranch = (Stmt) visitStatement(ctx.statement(1));
    }

    IfStmt ifStmt = new IfStmt(condition, ifBranch, elseBranch);

    return ifStmt;
  }

  @Override
  public Object visitLiteral(LiteralContext ctx) {
    // TODO Auto-generated method stub
    return super.visitLiteral(ctx);
  }

  @Override
  public Object visitLogicalAndExpr(LogicalAndExprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitLogicalAndExpr(ctx);
  }

  @Override
  public Object visitLogicalOrExpr(LogicalOrExprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitLogicalOrExpr(ctx);
  }

  @Override
  public Object visitMultiplicativeExpr(MultiplicativeExprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitMultiplicativeExpr(ctx);
  }

  @Override
  public Object visitParam(ParamContext ctx) {
    Type type = null;
    if (ctx.typeRef() == null) {
      type = (Type) visitType(ctx.type());
    } else {
      type = (ReferenceType) visitTypeRef(ctx.typeRef());
    }

    String name = ctx.Identifier().getText();

    Parameter parameter = new Parameter(type, name);

    return parameter;
  }

  @Override
  public Object visitParamList(ParamListContext ctx) {
    List<Parameter> parameters = new ArrayList<>();

    if (ctx == null)
      return parameters;

    for (ParamContext param : ctx.param()) {
      parameters.add((Parameter) visitParam(param));
    }

    return parameters;
  }

  @Override
  public Object visitPostfixExpr(PostfixExprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitPostfixExpr(ctx);
  }

  @Override
  public Object visitPostfixPart(PostfixPartContext ctx) {
    // TODO Auto-generated method stub
    return super.visitPostfixPart(ctx);
  }

  @Override
  public Object visitPrimaryExpr(PrimaryExprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitPrimaryExpr(ctx);
  }

  @Override
  public Object visitProgram(ProgramContext ctx) {
    List<FunctionDef> functioDefs = new ArrayList<>();
    List<ClassDef> classDefs = new ArrayList<>();

    for (DeclarationContext decl : ctx.declaration()) {
      Object declaration = visitDeclaration(decl);
      if (declaration != null && declaration instanceof FunctionDef)
        functioDefs.add((FunctionDef) declaration);

      if (declaration != null && declaration instanceof ClassDef)
        classDefs.add((ClassDef) declaration);
    }

    return new Program(functioDefs, classDefs);
  }

  @Override
  public Object visitRelationalExpr(RelationalExprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitRelationalExpr(ctx);
  }

  @Override
  public Object visitReturnStmt(ReturnStmtContext ctx) {
    Expr returnValue = (Expr) visitExpr(ctx.expr());
    ReturnStmt returnStmt = new ReturnStmt(returnValue);

    return returnStmt;
  }

  @Override
  public Object visitStatement(StatementContext ctx) {

    if (ctx.block() != null) {
      return (BlockStmt) visitBlock(ctx.block());
    } else if (ctx.varDecl() != null) {
      return (VariableStmt) visitVarDecl(ctx.varDecl());
    } else if (ctx.ifStmt() != null) {
      return (IfStmt) visitIfStmt(ctx.ifStmt());
    } else if (ctx.whileStmt() != null) {
      return (WhileStmt) visitWhileStmt(ctx.whileStmt());
    } else if (ctx.returnStmt() != null) {
      return (ReturnStmt) visitReturnStmt(ctx.returnStmt());
    } else if (ctx.expr() != null) {
      Expr expression = (Expr) visitExpr(ctx.expr());
      ExprStmt exprStmt = new ExprStmt(expression);
      return exprStmt;
    }

    return null;
  }

  @Override
  public Object visitType(TypeContext ctx) {
    if (ctx.baseType() != null) {
      return visitBaseType(ctx.baseType());
    }

    ClassType classType = new ClassType(ctx.Identifier().getText());

    return classType;
  }

  @Override
  public Object visitTypeRef(TypeRefContext ctx) {
    Type type = (Type) visitType(ctx.type());
    ReferenceType referenceType = new ReferenceType(type);

    return referenceType;
  }

  @Override
  public Object visitUnaryExpr(UnaryExprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitUnaryExpr(ctx);
  }

  @Override
  public Object visitVarDecl(VarDeclContext ctx) {
    Type type = null;

    if (ctx.type() == null) {
      type = (Type) visitTypeRef(ctx.typeRef());
    }

    if (ctx.typeRef() == null) {
      type = (Type) visitType(ctx.type());
    }

    String name = ctx.Identifier().getText();
    Expr value = null;

    if (ctx.ASSIGN() != null) {
      value = (Expr) visitExpr(ctx.expr());
    }

    VariableStmt variableStmt = new VariableStmt(type, name, value);

    return variableStmt;
  }

  @Override
  public Object visitWhileStmt(WhileStmtContext ctx) {
    Expr condition = (Expr) visitExpr(ctx.expr());
    Stmt body = (Stmt) visitStatement(ctx.statement());

    WhileStmt whileStmt = new WhileStmt(condition, body);

    return whileStmt;
  }

}
