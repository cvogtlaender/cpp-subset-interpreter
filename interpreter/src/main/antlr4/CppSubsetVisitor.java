// Generated from ./interpreter/src/main/antlr4/CppSubset.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CppSubsetParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CppSubsetVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(CppSubsetParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#topLevelDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTopLevelDecl(CppSubsetParser.TopLevelDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#functionDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDecl(CppSubsetParser.FunctionDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#paramList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamList(CppSubsetParser.ParamListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam(CppSubsetParser.ParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#classDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassDecl(CppSubsetParser.ClassDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#memberDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberDecl(CppSubsetParser.MemberDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#fieldDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldDecl(CppSubsetParser.FieldDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#methodDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodDecl(CppSubsetParser.MethodDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#constructorDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorDecl(CppSubsetParser.ConstructorDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#baseType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBaseType(CppSubsetParser.BaseTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#typeRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeRef(CppSubsetParser.TypeRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#returnType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnType(CppSubsetParser.ReturnTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(CppSubsetParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(CppSubsetParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#varDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDecl(CppSubsetParser.VarDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#exprStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprStmt(CppSubsetParser.ExprStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#ifStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStmt(CppSubsetParser.IfStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#whileStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStmt(CppSubsetParser.WhileStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#returnStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStmt(CppSubsetParser.ReturnStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(CppSubsetParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(CppSubsetParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#logicalOr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOr(CppSubsetParser.LogicalOrContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#logicalAnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAnd(CppSubsetParser.LogicalAndContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#equality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEquality(CppSubsetParser.EqualityContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#relational}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelational(CppSubsetParser.RelationalContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#addSub}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddSub(CppSubsetParser.AddSubContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#mulDivMod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulDivMod(CppSubsetParser.MulDivModContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary(CppSubsetParser.UnaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#postfix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfix(CppSubsetParser.PostfixContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#postfixSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixSuffix(CppSubsetParser.PostfixSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(CppSubsetParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link CppSubsetParser#argList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgList(CppSubsetParser.ArgListContext ctx);
}