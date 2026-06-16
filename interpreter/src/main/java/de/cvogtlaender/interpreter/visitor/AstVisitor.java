package de.cvogtlaender.interpreter.visitor;

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
import de.cvogtlaender.interpreter.ast.expression.ErrorExpr;
import de.cvogtlaender.interpreter.ast.expression.IntLiteral;
import de.cvogtlaender.interpreter.ast.expression.MemberAccessExpr;
import de.cvogtlaender.interpreter.ast.expression.StringLiteral;
import de.cvogtlaender.interpreter.ast.expression.UnaryExpr;
import de.cvogtlaender.interpreter.ast.expression.VarExpr;
import de.cvogtlaender.interpreter.ast.statement.BlockStmt;
import de.cvogtlaender.interpreter.ast.statement.ExprStmt;
import de.cvogtlaender.interpreter.ast.statement.IfStmt;
import de.cvogtlaender.interpreter.ast.statement.ReturnStmt;
import de.cvogtlaender.interpreter.ast.statement.VariableStmt;
import de.cvogtlaender.interpreter.ast.statement.WhileStmt;
import de.cvogtlaender.interpreter.ast.type.ClassType;
import de.cvogtlaender.interpreter.ast.type.PointerType;
import de.cvogtlaender.interpreter.ast.type.PrimitiveType;
import de.cvogtlaender.interpreter.ast.type.ReferenceType;

public interface AstVisitor<T> {

  T visitProgram(Program node);

  T visitClassDecl(ClassDecl node);

  T visitConstructorDecl(ConstructorDecl node);

  T visitFieldDecl(FieldDecl node);

  T visitFunctionDecl(FunctionDecl node);

  T visitMethodDecl(MethodDecl node);

  T visitParameterDecl(ParameterDecl node);

  T visitVariableDecl(VariableDecl node);

  T visitAssignExpr(AssignExpr node);

  T visitBinaryExpr(BinaryExpr node);

  T visitBoolLiteral(BoolLiteral node);

  T visitCallExpr(CallExpr node);

  T visitCharLiteral(CharLiteral node);

  T visitErrorExpr(ErrorExpr node);

  T visitIntLiteral(IntLiteral node);

  T visitMemberAccessExpr(MemberAccessExpr node);

  T visitStringLiteral(StringLiteral node);

  T visitUnaryExpr(UnaryExpr node);

  T visitVarExpr(VarExpr node);

  T visitBlockStmt(BlockStmt node);

  T visitExprStmt(ExprStmt node);

  T visitIfStmt(IfStmt node);

  T visitReturnStmt(ReturnStmt node);

  T visitVariableStmt(VariableStmt node);

  T visitWhileStmt(WhileStmt node);

  T visitClassType(ClassType node);

  T visitPointerType(PointerType node);

  T visitPrimitiveType(PrimitiveType node);

  T visitReferenceType(ReferenceType node);
}
