package de.cvogtlaender.interpreter.visitor;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import de.cvogtlaender.interpreter.ast.AstNode;
import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.ast.ReplInput;
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
import de.cvogtlaender.interpreter.ast.type.PointerType;
import de.cvogtlaender.interpreter.ast.type.PrimitiveType;
import de.cvogtlaender.interpreter.ast.type.ReferenceType;
import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.MiniCppBaseVisitor;
import de.cvogtlaender.interpreter.MiniCppParser.AdditiveExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.AssignmentExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.BaseTypeContext;
import de.cvogtlaender.interpreter.MiniCppParser.BlockContext;
import de.cvogtlaender.interpreter.MiniCppParser.ClassDefContext;
import de.cvogtlaender.interpreter.MiniCppParser.ConstructorDefContext;
import de.cvogtlaender.interpreter.MiniCppParser.DeclarationContext;
import de.cvogtlaender.interpreter.MiniCppParser.DeleteStmtContext;
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
import de.cvogtlaender.interpreter.MiniCppParser.NewExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.ParamContext;
import de.cvogtlaender.interpreter.MiniCppParser.PostfixExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.PostfixPartContext;
import de.cvogtlaender.interpreter.MiniCppParser.PrimaryExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.ProgramContext;
import de.cvogtlaender.interpreter.MiniCppParser.RelationalExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.ReplInputContext;
import de.cvogtlaender.interpreter.MiniCppParser.ReturnStmtContext;
import de.cvogtlaender.interpreter.MiniCppParser.StatementContext;
import de.cvogtlaender.interpreter.MiniCppParser.TypeContext;
import de.cvogtlaender.interpreter.MiniCppParser.TypeRefContext;
import de.cvogtlaender.interpreter.MiniCppParser.UnaryExprContext;
import de.cvogtlaender.interpreter.MiniCppParser.VarDeclContext;
import de.cvogtlaender.interpreter.MiniCppParser.WhileStmtContext;

/**
 * Builds the AST from an ANTLR parse tree. Must only be applied to parse
 * trees without syntax errors.
 */
public class ASTBuildVisitor extends MiniCppBaseVisitor<Object> {

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
        case FunctionDecl f -> functioDefs.add(f);
        case ClassDecl c -> classDefs.add(c);
        default -> {
          continue;
        }
      }
    }

    return at(new Program(functioDefs, classDefs), ctx);
  }

  @Override
  public Object visitReplInput(ReplInputContext ctx) {
    List<AstNode> items = new ArrayList<>();

    for (int i = 0; i < ctx.getChildCount(); i++) {
      if (ctx.getChild(i) instanceof DeclarationContext decl) {
        items.add((AstNode) visitDeclaration(decl));
      } else if (ctx.getChild(i) instanceof StatementContext statement) {
        items.add((AstNode) visit(statement));
      }
    }

    Expr trailing = ctx.expr() == null ? null : (Expr) visit(ctx.expr());

    return new ReplInput(items, trailing);
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
    FunctionDecl function = at(new FunctionDecl(returnType, name, parameters, body), ctx);

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
    ParameterDecl parameter = at(new ParameterDecl(type, name), ctx);

    return parameter;
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

    ClassDecl classDef = at(new ClassDecl(className, parentClassName, fields, constructors, methods), ctx);

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
    FieldDecl field = at(new FieldDecl(type, name), ctx);

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

    ConstructorDecl constructor = at(new ConstructorDecl(name, parameters, body), ctx);

    return constructor;

  }

  @Override
  public Object visitMethodDef(MethodDefContext ctx) {
    Type returnType = (Type) visit(ctx.type());
    String name = ctx.Identifier().getText();
    boolean isVirtual = ctx.VIRTUAL() != null;

    List<ParameterDecl> parameters = new ArrayList<>();

    if (ctx.paramList() != null) {
      for (ParamContext param : ctx.paramList().param()) {
        parameters.add((ParameterDecl) visit(param));
      }
    }

    BlockStmt body = (BlockStmt) visit(ctx.block());

    MethodDecl method = at(new MethodDecl(returnType, name, parameters, body, isVirtual), ctx);

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

    AssignExpr assignExpr = at(new AssignExpr(target, value), ctx);

    return assignExpr;
  }

  @Override
  public Object visitLogicalOrExpr(LogicalOrExprContext ctx) {

    Expr expr = (Expr) visit(ctx.logicalAndExpr(0));

    for (int i = 1; i < ctx.logicalAndExpr().size(); i++) {
      Expr right = (Expr) visit(ctx.logicalAndExpr(i));
      BinaryExpr.Operator op = BinaryExpr.Operator.OR;

      expr = span(new BinaryExpr(op, expr, right), expr, right);
    }

    return expr;
  }

  @Override
  public Object visitLogicalAndExpr(LogicalAndExprContext ctx) {

    Expr expr = (Expr) visit(ctx.equalityExpr(0));

    for (int i = 1; i < ctx.equalityExpr().size(); i++) {
      Expr right = (Expr) visit(ctx.equalityExpr(i));
      BinaryExpr.Operator op = BinaryExpr.Operator.AND;

      expr = span(new BinaryExpr(op, expr, right), expr, right);
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

      expr = span(new BinaryExpr(op, expr, right), expr, right);
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

      expr = span(new BinaryExpr(op, expr, right), expr, right);
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

      expr = span(new BinaryExpr(op, expr, right), expr, right);
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

      expr = span(new BinaryExpr(op, expr, right), expr, right);
    }

    return expr;
  }

  @Override
  public Object visitUnaryExpr(UnaryExprContext ctx) {

    if (ctx.postfixExpr() != null) {
      return visit(ctx.postfixExpr());
    }

    if (ctx.newExpr() != null) {
      return visit(ctx.newExpr());
    }

    Expr unaryExpr = (Expr) visit(ctx.unaryExpr());

    UnaryExpr.Operator op = UnaryExpr.Operator.POSITIVE;

    if (ctx.MINUS() != null) {
      op = UnaryExpr.Operator.NEGATE;
    } else if (ctx.NOT() != null) {
      op = UnaryExpr.Operator.NOT;
    } else if (ctx.STAR() != null) {
      op = UnaryExpr.Operator.DEREF;
    } else if (ctx.AMP() != null) {
      op = UnaryExpr.Operator.ADDRESS_OF;
    }

    // fold '-2147483648', which is out of range as a positive literal
    if (op == UnaryExpr.Operator.NEGATE && unaryExpr instanceof ErrorExpr
        && ctx.unaryExpr().getText().equals("2147483648")) {
      return at(new IntLiteral(Integer.MIN_VALUE), ctx);
    }

    Expr expr = at(new UnaryExpr(op, unaryExpr), ctx);

    return expr;
  }

  @Override
  public Object visitNewExpr(NewExprContext ctx) {
    Type type = ctx.baseType() != null
        ? (Type) visit(ctx.baseType())
        : new ClassType(ctx.Identifier().getText());

    List<Expr> args = new ArrayList<>();

    if (ctx.argList() != null) {
      for (ExprContext e : ctx.argList().expr()) {
        args.add((Expr) visit(e));
      }
    }

    NewExpr newExpr = at(new NewExpr(type, args), ctx);

    return newExpr;
  }

  @Override
  public Object visitPostfixExpr(PostfixExprContext ctx) {
    Expr expr = (Expr) visit(ctx.primaryExpr());

    for (PostfixPartContext part : ctx.postfixPart()) {

      if (part.Identifier() != null) {

        expr = span(new MemberAccessExpr(
            expr,
            part.Identifier().getText(),
            part.ARROW() != null), expr, part);

      } else {

        List<Expr> args = new ArrayList<>();

        if (part.argList() != null) {
          for (ExprContext e : part.argList().expr()) {
            args.add((Expr) visit(e));
          }
        }

        expr = span(new CallExpr(
            expr,
            args), expr, part);
      }
    }

    return expr;
  }

  @Override
  public Object visitPrimaryExpr(PrimaryExprContext ctx) {

    if (ctx.literal() != null) {
      return (Expr) visit(ctx.literal());
    }

    if (ctx.Identifier() != null) {
      return at(new VarExpr(ctx.Identifier().getText()), ctx);
    }

    return (Expr) visit(ctx.expr());
  }

  @Override
  public Object visitLiteral(LiteralContext ctx) {
    if (ctx.IntLiteral() != null) {
      String text = ctx.IntLiteral().getText();

      try {
        return at(new IntLiteral(Integer.parseInt(text)), ctx);
      } catch (NumberFormatException e) {
        return at(new ErrorExpr("integer literal '" + text + "' is out of range for int"), ctx);
      }
    }

    if (ctx.BoolLiteral() != null) {
      return at(new BoolLiteral(
          Boolean.parseBoolean(
              ctx.BoolLiteral().getText())),
          ctx);
    }

    if (ctx.NULLPTR() != null) {
      return at(new NullptrLiteral(), ctx);
    }

    if (ctx.StringLiteral() != null) {
      String text = ctx.StringLiteral().getText();
      return at(new StringLiteral(unescape(text.substring(1, text.length() - 1))), ctx);
    }

    String text = ctx.CharLiteral().getText();
    return at(new CharLiteral(unescape(text.substring(1, text.length() - 1)).charAt(0)), ctx);
  }

  // Statement

  @Override
  public Object visitStatement(StatementContext ctx) {

    if (ctx.block() != null) {
      return (BlockStmt) visit(ctx.block());
    } else if (ctx.varDecl() != null) {
      return at((VariableStmt) visit(ctx.varDecl()), ctx);
    } else if (ctx.ifStmt() != null) {
      return (IfStmt) visit(ctx.ifStmt());
    } else if (ctx.whileStmt() != null) {
      return (WhileStmt) visit(ctx.whileStmt());
    } else if (ctx.returnStmt() != null) {
      return at((ReturnStmt) visit(ctx.returnStmt()), ctx);
    } else if (ctx.deleteStmt() != null) {
      return at((DeleteStmt) visit(ctx.deleteStmt()), ctx);
    } else if (ctx.expr() != null) {
      Expr expression = (Expr) visit(ctx.expr());
      ExprStmt exprStmt = at(new ExprStmt(expression), ctx);
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

    BlockStmt blockStmt = at(new BlockStmt(statements), ctx);

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

    IfStmt ifStmt = at(new IfStmt(condition, ifBranch, elseBranch), ctx);

    return ifStmt;
  }

  @Override
  public Object visitWhileStmt(WhileStmtContext ctx) {
    Expr condition = (Expr) visit(ctx.expr());
    Stmt body = (Stmt) visit(ctx.statement());

    WhileStmt whileStmt = at(new WhileStmt(condition, body), ctx);

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
    } else if (ctx.argList() != null) {
      // direct initialization 'T x(a, b);' is sugar for 'T x = T(a, b);'
      List<Expr> args = new ArrayList<>();

      for (ExprContext e : ctx.argList().expr()) {
        args.add((Expr) visit(e));
      }

      if (type instanceof ClassType) {
        VarExpr typeName = at(new VarExpr(type.getName()), ctx.type());
        value = at(new CallExpr(typeName, args), ctx);
      } else {
        value = at(new ErrorExpr("direct initialization 'T x(...)' is only supported for class types"), ctx);
      }
    }

    VariableDecl decl = at(new VariableDecl(name, type, value), ctx);
    VariableStmt variableStmt = at(new VariableStmt(decl), ctx);

    return variableStmt;
  }

  @Override
  public Object visitReturnStmt(ReturnStmtContext ctx) {
    Expr returnValue = ctx.expr() == null ? null : (Expr) visit(ctx.expr());
    ReturnStmt returnStmt = at(new ReturnStmt(returnValue), ctx);

    return returnStmt;
  }

  @Override
  public Object visitDeleteStmt(DeleteStmtContext ctx) {
    Expr pointer = (Expr) visit(ctx.expr());
    DeleteStmt deleteStmt = at(new DeleteStmt(pointer), ctx);

    return deleteStmt;
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
    Type type = ctx.baseType() != null
        ? (Type) visit(ctx.baseType())
        : new ClassType(ctx.Identifier().getText());

    for (int i = 0; i < ctx.STAR().size(); i++) {
      type = new PointerType(type);
    }

    return type;
  }

  @Override
  public Object visitTypeRef(TypeRefContext ctx) {
    Type type = (Type) visit(ctx.type());
    ReferenceType referenceType = new ReferenceType(type);

    return referenceType;
  }

  // Helpers

  private static <N extends AstNode> N at(N node, ParserRuleContext ctx) {
    Token start = ctx.getStart();
    Token stop = ctx.getStop() != null ? ctx.getStop() : start;
    node.setRange(start.getLine(), start.getCharPositionInLine(),
        stop.getLine(), stop.getCharPositionInLine() + Math.max(1, stop.getText().length()));
    return node;
  }

  // range from the start of 'from' to the end of 'to'
  private static <N extends AstNode> N span(N node, AstNode from, ParserRuleContext to) {
    at(node, to);
    node.setRange(from.getLine(), from.getColumn(), node.getEndLine(), node.getEndColumn());
    return node;
  }

  private static <N extends AstNode> N span(N node, AstNode from, AstNode to) {
    node.setRange(from.getLine(), from.getColumn(), to.getEndLine(), to.getEndColumn());
    return node;
  }

  public static String unescape(String body) {
    StringBuilder out = new StringBuilder();

    for (int i = 0; i < body.length(); i++) {
      char c = body.charAt(i);

      if (c != '\\' || i + 1 >= body.length()) {
        out.append(c);
        continue;
      }

      char escaped = body.charAt(++i);
      out.append(switch (escaped) {
        case 'n' -> '\n';
        case 't' -> '\t';
        case 'r' -> '\r';
        case 'b' -> '\b';
        case '0' -> '\0';
        default -> escaped;
      });
    }

    return out.toString();
  }
}
