package de.cvogtlaender.interpreter;

import java.util.ArrayList;
import java.util.List;

import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.ConstructorDecl;
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
import de.cvogtlaender.interpreter.ast.type.PrimitiveType;
import de.cvogtlaender.interpreter.ast.type.ReferenceType;
import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.MiniCppParser.AdditiveExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.ArgListContext;
import de.cvogtlaender.interpreter.MiniCppParser.AssignmentExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.BaseTypeContext;
import de.cvogtlaender.interpreter.MiniCppParser.BlockContext;
import de.cvogtlaender.interpreter.MiniCppParser.ClassDefContext;
import de.cvogtlaender.interpreter.MiniCppParser.ConstructorDefContext;
import de.cvogtlaender.interpreter.MiniCppParser.DeclarationContext;
import de.cvogtlaender.interpreter.MiniCppParser.EqualityExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.ExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.FieldDeclContext;
import de.cvogtlaender.interpreter.MiniCppParser.FunctionDefContext;
import de.cvogtlaender.interpreter.MiniCppParser.IfStmtContext;
import de.cvogtlaender.interpreter.MiniCppParser.LiteralContext;
import de.cvogtlaender.interpreter.MiniCppParser.LogicalAndExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.LogicalOrExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.MemberDeclContext;
import de.cvogtlaender.interpreter.MiniCppParser.MethodDefContext;
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
  public Object visitProgram(ProgramContext ctx) {
    List<FunctionDecl> functioDefs = new ArrayList<>();
    List<ClassDecl> classDefs = new ArrayList<>();

    for (DeclarationContext decl : ctx.declaration()) {
      Object declaration = visitDeclaration(decl);

      if (declaration == null) {
        continue;
      }

      switch (declaration) {
        case FunctionDecl f -> functioDefs.add((FunctionDecl) declaration);
        case ClassDecl c -> classDefs.add((ClassDecl) declaration);
        default -> {
          continue;
        }
      }
    }

    return new Program(functioDefs, classDefs);
  }

  // Declaration

  @Override
  public Object visitDeclaration(DeclarationContext ctx) {
    if (ctx.functionDef() != null) {
      return visit(ctx.functionDef());
    }

    return visit(ctx.classDef());
  }

  @Override
  public Object visitFunctionDef(FunctionDefContext ctx) {
    Type returnType = (Type) visit(ctx.type());
    String name = ctx.Identifier().getText();

    List<ParameterDecl> parameters = new ArrayList<>();

    if (ctx.paramList() != null) {
      for (ParamContext param : ctx.paramList().param()) {
        parameters.add((ParameterDecl) visit(param));
      }
    }

    BlockStmt body = (BlockStmt) visit(ctx.block());
    FunctionDecl function = new FunctionDecl(returnType, name, parameters, body);

    return function;
  }

  @Override
  public Object visitParam(ParamContext ctx) {
    Type type = null;

    if (ctx.typeRef() == null) {
      type = (Type) visit(ctx.type());
    } else {
      type = (ReferenceType) visit(ctx.typeRef());
    }

    String name = ctx.Identifier().getText();
    ParameterDecl parameter = new ParameterDecl(type, name);

    return parameter;
  }

  @Override
  public Object visitParamList(ParamListContext ctx) {
    return this.visitParamList(ctx);
  }

  @Override
  public Object visitClassDef(ClassDefContext ctx) {

    String className = ctx.Identifier(0).getText();
    String parentClassName = null;

    if (ctx.Identifier().size() > 1) {
      parentClassName = ctx.Identifier(1).getText();
    }

    List<FieldDecl> fields = new ArrayList<>();
    List<ConstructorDecl> constructors = new ArrayList<>();
    List<MethodDecl> methods = new ArrayList<>();

    for (MemberDeclContext member : ctx.memberDecl()) {
      Object obj = visit(member);

      if (obj == null) {
        continue;
      }

      switch (obj) {
        case FieldDecl p -> fields.add(p);
        case ConstructorDecl c -> constructors.add(c);
        case MethodDecl m -> methods.add(m);
        default -> {
        }
      }
    }

    ClassDecl classDef = new ClassDecl(className, parentClassName, fields, constructors, methods);

    return classDef;
  }

  @Override
  public Object visitMemberDecl(MemberDeclContext ctx) {

    if (ctx.fieldDecl() != null) {
      return visit(ctx.fieldDecl());
    }

    if (ctx.methodDef() != null) {
      return visit(ctx.methodDef());
    }

    return visit(ctx.constructorDef());
  }

  @Override
  public Object visitFieldDecl(FieldDeclContext ctx) {
    Type type = (Type) visit(ctx.type());

    String name = ctx.Identifier().getText();
    FieldDecl field = new FieldDecl(type, name);

    return field;
  }

  @Override
  public Object visitConstructorDef(ConstructorDefContext ctx) {
    String name = ctx.Identifier().getText();

    List<ParameterDecl> parameters = new ArrayList<>();

    if (ctx.paramList() != null) {
      for (ParamContext param : ctx.paramList().param()) {
        parameters.add((ParameterDecl) visit(param));
      }
    }

    BlockStmt body = (BlockStmt) visit(ctx.block());

    ConstructorDecl constructor = new ConstructorDecl(name, parameters, body);

    return constructor;

  }

  @Override
  public Object visitMethodDef(MethodDefContext ctx) {
    Type returnType = (Type) visit(ctx.type());
    String name = ctx.Identifier().getText();
    Boolean isVirtual = ctx.VIRTUAL() == null ? false : true;

    List<ParameterDecl> parameters = new ArrayList<>();

    if (ctx.paramList() != null) {
      for (ParamContext param : ctx.paramList().param()) {
        parameters.add((ParameterDecl) visit(param));
      }
    }

    BlockStmt body = (BlockStmt) visit(ctx.block());

    MethodDecl method = new MethodDecl(returnType, name, parameters, body, isVirtual);

    return method;
  }

  // Expression

  @Override
  public Object visitExpr(ExprContext ctx) {
    return visit(ctx.assignmentExpr());
  }

  @Override
  public Object visitAssignmentExpr(AssignmentExprContext ctx) {
    if (ctx.assignmentExpr() == null)
      return visit(ctx.logicalOrExpr());

    Expr target = (Expr) visit(ctx.logicalOrExpr());
    Expr value = (Expr) visit(ctx.assignmentExpr());

    AssignExpr assignExpr = new AssignExpr(target, value);

    return assignExpr;
  }

  @Override
  public Object visitLogicalOrExpr(LogicalOrExprContext ctx) {

    Expr expr = (Expr) visit(ctx.logicalAndExpr(0));

    for (int i = 1; i < ctx.logicalAndExpr().size(); i++) {
      Expr right = (Expr) visit(ctx.logicalAndExpr(i));
      BinaryExpr.Operator op = BinaryExpr.Operator.OR;

      expr = new BinaryExpr(op, expr, right);
    }

    return expr;
  }

  @Override
  public Object visitLogicalAndExpr(LogicalAndExprContext ctx) {

    Expr expr = (Expr) visit(ctx.equalityExpr(0));

    for (int i = 1; i < ctx.equalityExpr().size(); i++) {
      Expr right = (Expr) visit(ctx.equalityExpr(i));
      BinaryExpr.Operator op = BinaryExpr.Operator.AND;

      expr = new BinaryExpr(op, expr, right);
    }

    return expr;
  }

  @Override
  public Object visitEqualityExpr(EqualityExprContext ctx) {

    Expr expr = (Expr) visit(ctx.relationalExpr(0));

    for (int i = 1; i < ctx.relationalExpr().size(); i++) {
      Expr right = (Expr) visit(ctx.relationalExpr(i));

      String opText = ctx.getChild(i * 2 - 1).getText();

      BinaryExpr.Operator op = switch (opText) {
        case "==" -> BinaryExpr.Operator.EQ;
        case "!=" -> BinaryExpr.Operator.NEQ;
        default -> throw new IllegalStateException("Unkown Operator: " + opText);
      };

      expr = new BinaryExpr(op, expr, right);
    }

    return expr;
  }

  @Override
  public Object visitRelationalExpr(RelationalExprContext ctx) {
    Expr expr = (Expr) visit(ctx.additiveExpr(0));

    for (int i = 1; i < ctx.additiveExpr().size(); i++) {
      Expr right = (Expr) visit(ctx.additiveExpr(i));

      String opText = ctx.getChild(i * 2 - 1).getText();

      BinaryExpr.Operator op = switch (opText) {
        case ">=" -> BinaryExpr.Operator.GE;
        case ">" -> BinaryExpr.Operator.GT;
        case "<=" -> BinaryExpr.Operator.LE;
        case "<" -> BinaryExpr.Operator.LT;
        default -> throw new IllegalStateException("Unkown Operator: " + opText);
      };

      expr = new BinaryExpr(op, expr, right);
    }

    return expr;
  }

  @Override
  public Object visitAdditiveExpr(AdditiveExprContext ctx) {

    Expr expr = (Expr) visit(ctx.multiplicativeExpr(0));

    for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
      Expr right = (Expr) visit(ctx.multiplicativeExpr(i));

      String opText = ctx.getChild(i * 2 - 1).getText();

      BinaryExpr.Operator op = switch (opText) {
        case "+" -> BinaryExpr.Operator.ADD;
        case "-" -> BinaryExpr.Operator.SUB;
        default -> throw new IllegalStateException("Unkown Operator: " + opText);
      };

      expr = new BinaryExpr(op, expr, right);
    }

    return expr;
  }

  @Override
  public Object visitMultiplicativeExpr(MultiplicativeExprContext ctx) {

    Expr expr = (Expr) visit(ctx.unaryExpr(0));

    for (int i = 1; i < ctx.unaryExpr().size(); i++) {
      Expr right = (Expr) visit(ctx.unaryExpr(i));

      String opText = ctx.getChild(i * 2 - 1).getText();

      BinaryExpr.Operator op = switch (opText) {
        case "*" -> BinaryExpr.Operator.MUL;
        case "/" -> BinaryExpr.Operator.DIV;
        case "%" -> BinaryExpr.Operator.MOD;
        default -> throw new IllegalStateException("Unkown Operator: " + opText);
      };

      expr = new BinaryExpr(op, expr, right);
    }

    return expr;
  }

  @Override
  public Object visitUnaryExpr(UnaryExprContext ctx) {

    if (ctx.postfixExpr() != null) {
      return visit(ctx.postfixExpr());
    }

    Expr unaryExpr = (Expr) visit(ctx.unaryExpr());

    UnaryExpr.Operator op = UnaryExpr.Operator.POSITIVE;

    if (ctx.MINUS() != null) {
      op = UnaryExpr.Operator.NEGATE;
    } else if (ctx.NOT() != null) {
      op = UnaryExpr.Operator.NOT;
    } else {
      throw new IllegalStateException("Unkown Operator: " + ctx.getChild(1).toString());
    }

    Expr expr = new UnaryExpr(op, unaryExpr);

    return expr;
  }

  @Override
  public Object visitPostfixExpr(PostfixExprContext ctx) {
    Expr expr = (Expr) visit(ctx.primaryExpr());

    for (PostfixPartContext part : ctx.postfixPart()) {

      if (part.Identifier() != null) {

        expr = new MemberAccessExpr(
            expr,
            part.Identifier().getText());

      } else {

        List<Expr> args = new ArrayList<>();

        if (part.argList() != null) {
          for (ExprContext e : part.argList().expr()) {
            args.add((Expr) visit(e));
          }
        }

        expr = new CallExpr(
            expr,
            args);
      }
    }

    return expr;
  }

  @Override
  public Object visitPostfixPart(PostfixPartContext ctx) {
    return super.visitPostfixPart(ctx);
  }

  @Override
  public Object visitPrimaryExpr(PrimaryExprContext ctx) {

    if (ctx.literal() != null) {
      return (Expr) visit(ctx.literal());
    }

    if (ctx.Identifier() != null) {
      return new VarExpr(ctx.Identifier().getText());
    }

    return (Expr) visit(ctx.expr());
  }

  @Override
  public Object visitArgList(ArgListContext ctx) {
    return super.visitArgList(ctx);
  }

  @Override
  public Object visitLiteral(LiteralContext ctx) {
    if (ctx.IntLiteral() != null) {
      return new IntLiteral(
          Integer.parseInt(
              ctx.IntLiteral().getText()));
    }

    if (ctx.BoolLiteral() != null) {
      return new BoolLiteral(
          Boolean.parseBoolean(
              ctx.BoolLiteral().getText()));
    }

    if (ctx.StringLiteral() != null) {
      return new StringLiteral(ctx.StringLiteral().getText());
    }

    return new CharLiteral(ctx.CharLiteral().getText().charAt(0));
  }

  // Statement

  @Override
  public Object visitStatement(StatementContext ctx) {

    if (ctx.block() != null) {
      return (BlockStmt) visit(ctx.block());
    } else if (ctx.varDecl() != null) {
      return (VariableStmt) visit(ctx.varDecl());
    } else if (ctx.ifStmt() != null) {
      return (IfStmt) visit(ctx.ifStmt());
    } else if (ctx.whileStmt() != null) {
      return (WhileStmt) visit(ctx.whileStmt());
    } else if (ctx.returnStmt() != null) {
      return (ReturnStmt) visit(ctx.returnStmt());
    } else if (ctx.expr() != null) {
      Expr expression = (Expr) visit(ctx.expr());
      ExprStmt exprStmt = new ExprStmt(expression);
      return exprStmt;
    }

    return null;
  }

  @Override
  public Object visitBlock(BlockContext ctx) {
    List<Stmt> statements = new ArrayList<>();

    for (StatementContext statement : ctx.statement()) {
      statements.add((Stmt) visit(statement));
    }

    BlockStmt blockStmt = new BlockStmt(statements);

    return blockStmt;
  }

  @Override
  public Object visitIfStmt(IfStmtContext ctx) {
    Expr condition = (Expr) visit(ctx.expr());
    Stmt ifBranch = (Stmt) visit(ctx.statement(0));
    Stmt elseBranch = null;

    if (ctx.statement().size() > 1) {
      elseBranch = (Stmt) visit(ctx.statement(1));
    }

    IfStmt ifStmt = new IfStmt(condition, ifBranch, elseBranch);

    return ifStmt;
  }

  @Override
  public Object visitWhileStmt(WhileStmtContext ctx) {
    Expr condition = (Expr) visit(ctx.expr());
    Stmt body = (Stmt) visit(ctx.statement());

    WhileStmt whileStmt = new WhileStmt(condition, body);

    return whileStmt;
  }

  @Override
  public Object visitVarDecl(VarDeclContext ctx) {
    Type type = null;

    if (ctx.type() == null) {
      type = (Type) visit(ctx.typeRef());
    }

    if (ctx.typeRef() == null) {
      type = (Type) visit(ctx.type());
    }

    String name = ctx.Identifier().getText();
    Expr value = null;

    if (ctx.ASSIGN() != null) {
      value = (Expr) visit(ctx.expr());
    }

    VariableStmt variableStmt = new VariableStmt(new VariableDecl(name, type, value));

    return variableStmt;
  }

  @Override
  public Object visitReturnStmt(ReturnStmtContext ctx) {
    Expr returnValue = (Expr) visit(ctx.expr());
    ReturnStmt returnStmt = new ReturnStmt(returnValue);

    return returnStmt;
  }

  // Other

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
  public Object visitType(TypeContext ctx) {
    if (ctx.baseType() != null) {
      return visit(ctx.baseType());
    }

    ClassType classType = new ClassType(ctx.Identifier().getText());

    return classType;
  }

  @Override
  public Object visitTypeRef(TypeRefContext ctx) {
    Type type = (Type) visit(ctx.type());
    ReferenceType referenceType = new ReferenceType(type);

    return referenceType;
  }

}