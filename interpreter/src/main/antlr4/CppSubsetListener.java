// Generated from ./CppSubset.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CppSubsetParser}.
 */
public interface CppSubsetListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(CppSubsetParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(CppSubsetParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#topLevelDecl}.
	 * @param ctx the parse tree
	 */
	void enterTopLevelDecl(CppSubsetParser.TopLevelDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#topLevelDecl}.
	 * @param ctx the parse tree
	 */
	void exitTopLevelDecl(CppSubsetParser.TopLevelDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#functionDecl}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDecl(CppSubsetParser.FunctionDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#functionDecl}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDecl(CppSubsetParser.FunctionDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#paramList}.
	 * @param ctx the parse tree
	 */
	void enterParamList(CppSubsetParser.ParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#paramList}.
	 * @param ctx the parse tree
	 */
	void exitParamList(CppSubsetParser.ParamListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#param}.
	 * @param ctx the parse tree
	 */
	void enterParam(CppSubsetParser.ParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#param}.
	 * @param ctx the parse tree
	 */
	void exitParam(CppSubsetParser.ParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#classDecl}.
	 * @param ctx the parse tree
	 */
	void enterClassDecl(CppSubsetParser.ClassDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#classDecl}.
	 * @param ctx the parse tree
	 */
	void exitClassDecl(CppSubsetParser.ClassDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#memberDecl}.
	 * @param ctx the parse tree
	 */
	void enterMemberDecl(CppSubsetParser.MemberDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#memberDecl}.
	 * @param ctx the parse tree
	 */
	void exitMemberDecl(CppSubsetParser.MemberDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#fieldDecl}.
	 * @param ctx the parse tree
	 */
	void enterFieldDecl(CppSubsetParser.FieldDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#fieldDecl}.
	 * @param ctx the parse tree
	 */
	void exitFieldDecl(CppSubsetParser.FieldDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#methodDecl}.
	 * @param ctx the parse tree
	 */
	void enterMethodDecl(CppSubsetParser.MethodDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#methodDecl}.
	 * @param ctx the parse tree
	 */
	void exitMethodDecl(CppSubsetParser.MethodDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#constructorDecl}.
	 * @param ctx the parse tree
	 */
	void enterConstructorDecl(CppSubsetParser.ConstructorDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#constructorDecl}.
	 * @param ctx the parse tree
	 */
	void exitConstructorDecl(CppSubsetParser.ConstructorDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#baseType}.
	 * @param ctx the parse tree
	 */
	void enterBaseType(CppSubsetParser.BaseTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#baseType}.
	 * @param ctx the parse tree
	 */
	void exitBaseType(CppSubsetParser.BaseTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#typeRef}.
	 * @param ctx the parse tree
	 */
	void enterTypeRef(CppSubsetParser.TypeRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#typeRef}.
	 * @param ctx the parse tree
	 */
	void exitTypeRef(CppSubsetParser.TypeRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#returnType}.
	 * @param ctx the parse tree
	 */
	void enterReturnType(CppSubsetParser.ReturnTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#returnType}.
	 * @param ctx the parse tree
	 */
	void exitReturnType(CppSubsetParser.ReturnTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(CppSubsetParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(CppSubsetParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(CppSubsetParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(CppSubsetParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void enterVarDecl(CppSubsetParser.VarDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void exitVarDecl(CppSubsetParser.VarDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#exprStmt}.
	 * @param ctx the parse tree
	 */
	void enterExprStmt(CppSubsetParser.ExprStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#exprStmt}.
	 * @param ctx the parse tree
	 */
	void exitExprStmt(CppSubsetParser.ExprStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#ifStmt}.
	 * @param ctx the parse tree
	 */
	void enterIfStmt(CppSubsetParser.IfStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#ifStmt}.
	 * @param ctx the parse tree
	 */
	void exitIfStmt(CppSubsetParser.IfStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#whileStmt}.
	 * @param ctx the parse tree
	 */
	void enterWhileStmt(CppSubsetParser.WhileStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#whileStmt}.
	 * @param ctx the parse tree
	 */
	void exitWhileStmt(CppSubsetParser.WhileStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#returnStmt}.
	 * @param ctx the parse tree
	 */
	void enterReturnStmt(CppSubsetParser.ReturnStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#returnStmt}.
	 * @param ctx the parse tree
	 */
	void exitReturnStmt(CppSubsetParser.ReturnStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(CppSubsetParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(CppSubsetParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(CppSubsetParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(CppSubsetParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOr(CppSubsetParser.LogicalOrContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOr(CppSubsetParser.LogicalOrContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAnd(CppSubsetParser.LogicalAndContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAnd(CppSubsetParser.LogicalAndContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#equality}.
	 * @param ctx the parse tree
	 */
	void enterEquality(CppSubsetParser.EqualityContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#equality}.
	 * @param ctx the parse tree
	 */
	void exitEquality(CppSubsetParser.EqualityContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#relational}.
	 * @param ctx the parse tree
	 */
	void enterRelational(CppSubsetParser.RelationalContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#relational}.
	 * @param ctx the parse tree
	 */
	void exitRelational(CppSubsetParser.RelationalContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#addSub}.
	 * @param ctx the parse tree
	 */
	void enterAddSub(CppSubsetParser.AddSubContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#addSub}.
	 * @param ctx the parse tree
	 */
	void exitAddSub(CppSubsetParser.AddSubContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#mulDivMod}.
	 * @param ctx the parse tree
	 */
	void enterMulDivMod(CppSubsetParser.MulDivModContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#mulDivMod}.
	 * @param ctx the parse tree
	 */
	void exitMulDivMod(CppSubsetParser.MulDivModContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterUnary(CppSubsetParser.UnaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitUnary(CppSubsetParser.UnaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#postfix}.
	 * @param ctx the parse tree
	 */
	void enterPostfix(CppSubsetParser.PostfixContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#postfix}.
	 * @param ctx the parse tree
	 */
	void exitPostfix(CppSubsetParser.PostfixContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#postfixSuffix}.
	 * @param ctx the parse tree
	 */
	void enterPostfixSuffix(CppSubsetParser.PostfixSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#postfixSuffix}.
	 * @param ctx the parse tree
	 */
	void exitPostfixSuffix(CppSubsetParser.PostfixSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(CppSubsetParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(CppSubsetParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link CppSubsetParser#argList}.
	 * @param ctx the parse tree
	 */
	void enterArgList(CppSubsetParser.ArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CppSubsetParser#argList}.
	 * @param ctx the parse tree
	 */
	void exitArgList(CppSubsetParser.ArgListContext ctx);
}