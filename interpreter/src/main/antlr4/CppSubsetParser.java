// Generated from ./CppSubset.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class CppSubsetParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		BOOL=1, INT=2, CHAR=3, STRING=4, VOID=5, CLASS=6, PUBLIC=7, VIRTUAL=8, 
		IF=9, ELSE=10, WHILE=11, RETURN=12, TRUE=13, FALSE=14, IDENT=15, INT_LIT=16, 
		CHAR_LIT=17, STRING_LIT=18, AND=19, OR=20, EQ=21, NEQ=22, LEQ=23, GEQ=24, 
		NOT=25, ASSIGN=26, LT=27, GT=28, PLUS=29, MINUS=30, STAR=31, SLASH=32, 
		PERCENT=33, REF=34, DOT=35, COMMA=36, SEMI=37, COLON=38, LPAREN=39, RPAREN=40, 
		LBRACE=41, RBRACE=42, WS=43, LINE_COMMENT=44, BLOCK_COMMENT=45, PREPROCESSOR=46;
	public static final int
		RULE_program = 0, RULE_topLevelDecl = 1, RULE_functionDecl = 2, RULE_paramList = 3, 
		RULE_param = 4, RULE_classDecl = 5, RULE_memberDecl = 6, RULE_fieldDecl = 7, 
		RULE_methodDecl = 8, RULE_constructorDecl = 9, RULE_baseType = 10, RULE_typeRef = 11, 
		RULE_returnType = 12, RULE_block = 13, RULE_statement = 14, RULE_varDecl = 15, 
		RULE_exprStmt = 16, RULE_ifStmt = 17, RULE_whileStmt = 18, RULE_returnStmt = 19, 
		RULE_expr = 20, RULE_assignment = 21, RULE_logicalOr = 22, RULE_logicalAnd = 23, 
		RULE_equality = 24, RULE_relational = 25, RULE_addSub = 26, RULE_mulDivMod = 27, 
		RULE_unary = 28, RULE_postfix = 29, RULE_postfixSuffix = 30, RULE_primary = 31, 
		RULE_argList = 32;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "topLevelDecl", "functionDecl", "paramList", "param", "classDecl", 
			"memberDecl", "fieldDecl", "methodDecl", "constructorDecl", "baseType", 
			"typeRef", "returnType", "block", "statement", "varDecl", "exprStmt", 
			"ifStmt", "whileStmt", "returnStmt", "expr", "assignment", "logicalOr", 
			"logicalAnd", "equality", "relational", "addSub", "mulDivMod", "unary", 
			"postfix", "postfixSuffix", "primary", "argList"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'bool'", "'int'", "'char'", "'string'", "'void'", "'class'", "'public'", 
			"'virtual'", "'if'", "'else'", "'while'", "'return'", "'true'", "'false'", 
			null, null, null, null, "'&&'", "'||'", "'=='", "'!='", "'<='", "'>='", 
			"'!'", "'='", "'<'", "'>'", "'+'", "'-'", "'*'", "'/'", "'%'", "'&'", 
			"'.'", "','", "';'", "':'", "'('", "')'", "'{'", "'}'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "BOOL", "INT", "CHAR", "STRING", "VOID", "CLASS", "PUBLIC", "VIRTUAL", 
			"IF", "ELSE", "WHILE", "RETURN", "TRUE", "FALSE", "IDENT", "INT_LIT", 
			"CHAR_LIT", "STRING_LIT", "AND", "OR", "EQ", "NEQ", "LEQ", "GEQ", "NOT", 
			"ASSIGN", "LT", "GT", "PLUS", "MINUS", "STAR", "SLASH", "PERCENT", "REF", 
			"DOT", "COMMA", "SEMI", "COLON", "LPAREN", "RPAREN", "LBRACE", "RBRACE", 
			"WS", "LINE_COMMENT", "BLOCK_COMMENT", "PREPROCESSOR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "CppSubset.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CppSubsetParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(CppSubsetParser.EOF, 0); }
		public List<TopLevelDeclContext> topLevelDecl() {
			return getRuleContexts(TopLevelDeclContext.class);
		}
		public TopLevelDeclContext topLevelDecl(int i) {
			return getRuleContext(TopLevelDeclContext.class,i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterProgram(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitProgram(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32894L) != 0)) {
				{
				{
				setState(66);
				topLevelDecl();
				}
				}
				setState(71);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(72);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TopLevelDeclContext extends ParserRuleContext {
		public FunctionDeclContext functionDecl() {
			return getRuleContext(FunctionDeclContext.class,0);
		}
		public ClassDeclContext classDecl() {
			return getRuleContext(ClassDeclContext.class,0);
		}
		public TopLevelDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_topLevelDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterTopLevelDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitTopLevelDecl(this);
		}
	}

	public final TopLevelDeclContext topLevelDecl() throws RecognitionException {
		TopLevelDeclContext _localctx = new TopLevelDeclContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_topLevelDecl);
		try {
			setState(76);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BOOL:
			case INT:
			case CHAR:
			case STRING:
			case VOID:
			case IDENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(74);
				functionDecl();
				}
				break;
			case CLASS:
				enterOuterAlt(_localctx, 2);
				{
				setState(75);
				classDecl();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionDeclContext extends ParserRuleContext {
		public ReturnTypeContext returnType() {
			return getRuleContext(ReturnTypeContext.class,0);
		}
		public TerminalNode IDENT() { return getToken(CppSubsetParser.IDENT, 0); }
		public TerminalNode LPAREN() { return getToken(CppSubsetParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(CppSubsetParser.RPAREN, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}
		public FunctionDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterFunctionDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitFunctionDecl(this);
		}
	}

	public final FunctionDeclContext functionDecl() throws RecognitionException {
		FunctionDeclContext _localctx = new FunctionDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_functionDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78);
			returnType();
			setState(79);
			match(IDENT);
			setState(80);
			match(LPAREN);
			setState(82);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32830L) != 0)) {
				{
				setState(81);
				paramList();
				}
			}

			setState(84);
			match(RPAREN);
			setState(85);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamListContext extends ParserRuleContext {
		public List<ParamContext> param() {
			return getRuleContexts(ParamContext.class);
		}
		public ParamContext param(int i) {
			return getRuleContext(ParamContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CppSubsetParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CppSubsetParser.COMMA, i);
		}
		public ParamListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_paramList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterParamList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitParamList(this);
		}
	}

	public final ParamListContext paramList() throws RecognitionException {
		ParamListContext _localctx = new ParamListContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_paramList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			param();
			setState(92);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(88);
				match(COMMA);
				setState(89);
				param();
				}
				}
				setState(94);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamContext extends ParserRuleContext {
		public TypeRefContext typeRef() {
			return getRuleContext(TypeRefContext.class,0);
		}
		public TerminalNode IDENT() { return getToken(CppSubsetParser.IDENT, 0); }
		public ParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitParam(this);
		}
	}

	public final ParamContext param() throws RecognitionException {
		ParamContext _localctx = new ParamContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_param);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(95);
			typeRef();
			setState(96);
			match(IDENT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassDeclContext extends ParserRuleContext {
		public TerminalNode CLASS() { return getToken(CppSubsetParser.CLASS, 0); }
		public List<TerminalNode> IDENT() { return getTokens(CppSubsetParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(CppSubsetParser.IDENT, i);
		}
		public TerminalNode LBRACE() { return getToken(CppSubsetParser.LBRACE, 0); }
		public List<TerminalNode> PUBLIC() { return getTokens(CppSubsetParser.PUBLIC); }
		public TerminalNode PUBLIC(int i) {
			return getToken(CppSubsetParser.PUBLIC, i);
		}
		public List<TerminalNode> COLON() { return getTokens(CppSubsetParser.COLON); }
		public TerminalNode COLON(int i) {
			return getToken(CppSubsetParser.COLON, i);
		}
		public TerminalNode RBRACE() { return getToken(CppSubsetParser.RBRACE, 0); }
		public TerminalNode SEMI() { return getToken(CppSubsetParser.SEMI, 0); }
		public List<MemberDeclContext> memberDecl() {
			return getRuleContexts(MemberDeclContext.class);
		}
		public MemberDeclContext memberDecl(int i) {
			return getRuleContext(MemberDeclContext.class,i);
		}
		public ClassDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterClassDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitClassDecl(this);
		}
	}

	public final ClassDeclContext classDecl() throws RecognitionException {
		ClassDeclContext _localctx = new ClassDeclContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_classDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(98);
			match(CLASS);
			setState(99);
			match(IDENT);
			setState(103);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(100);
				match(COLON);
				setState(101);
				match(PUBLIC);
				setState(102);
				match(IDENT);
				}
			}

			setState(105);
			match(LBRACE);
			setState(106);
			match(PUBLIC);
			setState(107);
			match(COLON);
			setState(111);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 33086L) != 0)) {
				{
				{
				setState(108);
				memberDecl();
				}
				}
				setState(113);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(114);
			match(RBRACE);
			setState(115);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MemberDeclContext extends ParserRuleContext {
		public FieldDeclContext fieldDecl() {
			return getRuleContext(FieldDeclContext.class,0);
		}
		public MethodDeclContext methodDecl() {
			return getRuleContext(MethodDeclContext.class,0);
		}
		public ConstructorDeclContext constructorDecl() {
			return getRuleContext(ConstructorDeclContext.class,0);
		}
		public MemberDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterMemberDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitMemberDecl(this);
		}
	}

	public final MemberDeclContext memberDecl() throws RecognitionException {
		MemberDeclContext _localctx = new MemberDeclContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_memberDecl);
		try {
			setState(120);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(117);
				fieldDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(118);
				methodDecl();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(119);
				constructorDecl();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldDeclContext extends ParserRuleContext {
		public BaseTypeContext baseType() {
			return getRuleContext(BaseTypeContext.class,0);
		}
		public TerminalNode IDENT() { return getToken(CppSubsetParser.IDENT, 0); }
		public TerminalNode SEMI() { return getToken(CppSubsetParser.SEMI, 0); }
		public FieldDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterFieldDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitFieldDecl(this);
		}
	}

	public final FieldDeclContext fieldDecl() throws RecognitionException {
		FieldDeclContext _localctx = new FieldDeclContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_fieldDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(122);
			baseType();
			setState(123);
			match(IDENT);
			setState(124);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MethodDeclContext extends ParserRuleContext {
		public ReturnTypeContext returnType() {
			return getRuleContext(ReturnTypeContext.class,0);
		}
		public TerminalNode IDENT() { return getToken(CppSubsetParser.IDENT, 0); }
		public TerminalNode LPAREN() { return getToken(CppSubsetParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(CppSubsetParser.RPAREN, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode VIRTUAL() { return getToken(CppSubsetParser.VIRTUAL, 0); }
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}
		public MethodDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterMethodDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitMethodDecl(this);
		}
	}

	public final MethodDeclContext methodDecl() throws RecognitionException {
		MethodDeclContext _localctx = new MethodDeclContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_methodDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VIRTUAL) {
				{
				setState(126);
				match(VIRTUAL);
				}
			}

			setState(129);
			returnType();
			setState(130);
			match(IDENT);
			setState(131);
			match(LPAREN);
			setState(133);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32830L) != 0)) {
				{
				setState(132);
				paramList();
				}
			}

			setState(135);
			match(RPAREN);
			setState(136);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstructorDeclContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(CppSubsetParser.IDENT, 0); }
		public TerminalNode LPAREN() { return getToken(CppSubsetParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(CppSubsetParser.RPAREN, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}
		public ConstructorDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constructorDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterConstructorDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitConstructorDecl(this);
		}
	}

	public final ConstructorDeclContext constructorDecl() throws RecognitionException {
		ConstructorDeclContext _localctx = new ConstructorDeclContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_constructorDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			match(IDENT);
			setState(139);
			match(LPAREN);
			setState(141);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32830L) != 0)) {
				{
				setState(140);
				paramList();
				}
			}

			setState(143);
			match(RPAREN);
			setState(144);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BaseTypeContext extends ParserRuleContext {
		public TerminalNode BOOL() { return getToken(CppSubsetParser.BOOL, 0); }
		public TerminalNode INT() { return getToken(CppSubsetParser.INT, 0); }
		public TerminalNode CHAR() { return getToken(CppSubsetParser.CHAR, 0); }
		public TerminalNode STRING() { return getToken(CppSubsetParser.STRING, 0); }
		public TerminalNode VOID() { return getToken(CppSubsetParser.VOID, 0); }
		public TerminalNode IDENT() { return getToken(CppSubsetParser.IDENT, 0); }
		public BaseTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_baseType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterBaseType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitBaseType(this);
		}
	}

	public final BaseTypeContext baseType() throws RecognitionException {
		BaseTypeContext _localctx = new BaseTypeContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_baseType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 32830L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeRefContext extends ParserRuleContext {
		public BaseTypeContext baseType() {
			return getRuleContext(BaseTypeContext.class,0);
		}
		public TerminalNode REF() { return getToken(CppSubsetParser.REF, 0); }
		public TypeRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterTypeRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitTypeRef(this);
		}
	}

	public final TypeRefContext typeRef() throws RecognitionException {
		TypeRefContext _localctx = new TypeRefContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_typeRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(148);
			baseType();
			setState(150);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==REF) {
				{
				setState(149);
				match(REF);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReturnTypeContext extends ParserRuleContext {
		public BaseTypeContext baseType() {
			return getRuleContext(BaseTypeContext.class,0);
		}
		public ReturnTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_returnType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterReturnType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitReturnType(this);
		}
	}

	public final ReturnTypeContext returnType() throws RecognitionException {
		ReturnTypeContext _localctx = new ReturnTypeContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_returnType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(152);
			baseType();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BlockContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(CppSubsetParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(CppSubsetParser.RBRACE, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitBlock(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			match(LBRACE);
			setState(158);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2750423759422L) != 0)) {
				{
				{
				setState(155);
				statement();
				}
				}
				setState(160);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(161);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public VarDeclContext varDecl() {
			return getRuleContext(VarDeclContext.class,0);
		}
		public ExprStmtContext exprStmt() {
			return getRuleContext(ExprStmtContext.class,0);
		}
		public IfStmtContext ifStmt() {
			return getRuleContext(IfStmtContext.class,0);
		}
		public WhileStmtContext whileStmt() {
			return getRuleContext(WhileStmtContext.class,0);
		}
		public ReturnStmtContext returnStmt() {
			return getRuleContext(ReturnStmtContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_statement);
		try {
			setState(169);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(163);
				varDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(164);
				exprStmt();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(165);
				ifStmt();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(166);
				whileStmt();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(167);
				returnStmt();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(168);
				block();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VarDeclContext extends ParserRuleContext {
		public TypeRefContext typeRef() {
			return getRuleContext(TypeRefContext.class,0);
		}
		public TerminalNode IDENT() { return getToken(CppSubsetParser.IDENT, 0); }
		public TerminalNode SEMI() { return getToken(CppSubsetParser.SEMI, 0); }
		public TerminalNode ASSIGN() { return getToken(CppSubsetParser.ASSIGN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public VarDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterVarDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitVarDecl(this);
		}
	}

	public final VarDeclContext varDecl() throws RecognitionException {
		VarDeclContext _localctx = new VarDeclContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_varDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
			typeRef();
			setState(172);
			match(IDENT);
			setState(175);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASSIGN) {
				{
				setState(173);
				match(ASSIGN);
				setState(174);
				expr();
				}
			}

			setState(177);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExprStmtContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(CppSubsetParser.SEMI, 0); }
		public ExprStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterExprStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitExprStmt(this);
		}
	}

	public final ExprStmtContext exprStmt() throws RecognitionException {
		ExprStmtContext _localctx = new ExprStmtContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_exprStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			expr();
			setState(180);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IfStmtContext extends ParserRuleContext {
		public TerminalNode IF() { return getToken(CppSubsetParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(CppSubsetParser.LPAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(CppSubsetParser.RPAREN, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(CppSubsetParser.ELSE, 0); }
		public IfStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterIfStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitIfStmt(this);
		}
	}

	public final IfStmtContext ifStmt() throws RecognitionException {
		IfStmtContext _localctx = new IfStmtContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_ifStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(182);
			match(IF);
			setState(183);
			match(LPAREN);
			setState(184);
			expr();
			setState(185);
			match(RPAREN);
			setState(186);
			statement();
			setState(189);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(187);
				match(ELSE);
				setState(188);
				statement();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhileStmtContext extends ParserRuleContext {
		public TerminalNode WHILE() { return getToken(CppSubsetParser.WHILE, 0); }
		public TerminalNode LPAREN() { return getToken(CppSubsetParser.LPAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(CppSubsetParser.RPAREN, 0); }
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public WhileStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whileStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterWhileStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitWhileStmt(this);
		}
	}

	public final WhileStmtContext whileStmt() throws RecognitionException {
		WhileStmtContext _localctx = new WhileStmtContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_whileStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(191);
			match(WHILE);
			setState(192);
			match(LPAREN);
			setState(193);
			expr();
			setState(194);
			match(RPAREN);
			setState(195);
			statement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReturnStmtContext extends ParserRuleContext {
		public TerminalNode RETURN() { return getToken(CppSubsetParser.RETURN, 0); }
		public TerminalNode SEMI() { return getToken(CppSubsetParser.SEMI, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ReturnStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_returnStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterReturnStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitReturnStmt(this);
		}
	}

	public final ReturnStmtContext returnStmt() throws RecognitionException {
		ReturnStmtContext _localctx = new ReturnStmtContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_returnStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
			match(RETURN);
			setState(199);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 551400497152L) != 0)) {
				{
				setState(198);
				expr();
				}
			}

			setState(201);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExprContext extends ParserRuleContext {
		public AssignmentContext assignment() {
			return getRuleContext(AssignmentContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203);
			assignment();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentContext extends ParserRuleContext {
		public LogicalOrContext logicalOr() {
			return getRuleContext(LogicalOrContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(CppSubsetParser.ASSIGN, 0); }
		public AssignmentContext assignment() {
			return getRuleContext(AssignmentContext.class,0);
		}
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitAssignment(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_assignment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(205);
			logicalOr();
			setState(208);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASSIGN) {
				{
				setState(206);
				match(ASSIGN);
				setState(207);
				assignment();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalOrContext extends ParserRuleContext {
		public List<LogicalAndContext> logicalAnd() {
			return getRuleContexts(LogicalAndContext.class);
		}
		public LogicalAndContext logicalAnd(int i) {
			return getRuleContext(LogicalAndContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(CppSubsetParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(CppSubsetParser.OR, i);
		}
		public LogicalOrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalOr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterLogicalOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitLogicalOr(this);
		}
	}

	public final LogicalOrContext logicalOr() throws RecognitionException {
		LogicalOrContext _localctx = new LogicalOrContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_logicalOr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(210);
			logicalAnd();
			setState(215);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(211);
				match(OR);
				setState(212);
				logicalAnd();
				}
				}
				setState(217);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalAndContext extends ParserRuleContext {
		public List<EqualityContext> equality() {
			return getRuleContexts(EqualityContext.class);
		}
		public EqualityContext equality(int i) {
			return getRuleContext(EqualityContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(CppSubsetParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(CppSubsetParser.AND, i);
		}
		public LogicalAndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalAnd; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterLogicalAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitLogicalAnd(this);
		}
	}

	public final LogicalAndContext logicalAnd() throws RecognitionException {
		LogicalAndContext _localctx = new LogicalAndContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_logicalAnd);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(218);
			equality();
			setState(223);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(219);
				match(AND);
				setState(220);
				equality();
				}
				}
				setState(225);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EqualityContext extends ParserRuleContext {
		public List<RelationalContext> relational() {
			return getRuleContexts(RelationalContext.class);
		}
		public RelationalContext relational(int i) {
			return getRuleContext(RelationalContext.class,i);
		}
		public List<TerminalNode> EQ() { return getTokens(CppSubsetParser.EQ); }
		public TerminalNode EQ(int i) {
			return getToken(CppSubsetParser.EQ, i);
		}
		public List<TerminalNode> NEQ() { return getTokens(CppSubsetParser.NEQ); }
		public TerminalNode NEQ(int i) {
			return getToken(CppSubsetParser.NEQ, i);
		}
		public EqualityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_equality; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterEquality(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitEquality(this);
		}
	}

	public final EqualityContext equality() throws RecognitionException {
		EqualityContext _localctx = new EqualityContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_equality);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(226);
			relational();
			setState(231);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==EQ || _la==NEQ) {
				{
				{
				setState(227);
				_la = _input.LA(1);
				if ( !(_la==EQ || _la==NEQ) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(228);
				relational();
				}
				}
				setState(233);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RelationalContext extends ParserRuleContext {
		public List<AddSubContext> addSub() {
			return getRuleContexts(AddSubContext.class);
		}
		public AddSubContext addSub(int i) {
			return getRuleContext(AddSubContext.class,i);
		}
		public List<TerminalNode> LT() { return getTokens(CppSubsetParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(CppSubsetParser.LT, i);
		}
		public List<TerminalNode> GT() { return getTokens(CppSubsetParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(CppSubsetParser.GT, i);
		}
		public List<TerminalNode> LEQ() { return getTokens(CppSubsetParser.LEQ); }
		public TerminalNode LEQ(int i) {
			return getToken(CppSubsetParser.LEQ, i);
		}
		public List<TerminalNode> GEQ() { return getTokens(CppSubsetParser.GEQ); }
		public TerminalNode GEQ(int i) {
			return getToken(CppSubsetParser.GEQ, i);
		}
		public RelationalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relational; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterRelational(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitRelational(this);
		}
	}

	public final RelationalContext relational() throws RecognitionException {
		RelationalContext _localctx = new RelationalContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_relational);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			addSub();
			setState(239);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 427819008L) != 0)) {
				{
				{
				setState(235);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 427819008L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(236);
				addSub();
				}
				}
				setState(241);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AddSubContext extends ParserRuleContext {
		public List<MulDivModContext> mulDivMod() {
			return getRuleContexts(MulDivModContext.class);
		}
		public MulDivModContext mulDivMod(int i) {
			return getRuleContext(MulDivModContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(CppSubsetParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(CppSubsetParser.PLUS, i);
		}
		public List<TerminalNode> MINUS() { return getTokens(CppSubsetParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(CppSubsetParser.MINUS, i);
		}
		public AddSubContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_addSub; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterAddSub(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitAddSub(this);
		}
	}

	public final AddSubContext addSub() throws RecognitionException {
		AddSubContext _localctx = new AddSubContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_addSub);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(242);
			mulDivMod();
			setState(247);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS || _la==MINUS) {
				{
				{
				setState(243);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(244);
				mulDivMod();
				}
				}
				setState(249);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MulDivModContext extends ParserRuleContext {
		public List<UnaryContext> unary() {
			return getRuleContexts(UnaryContext.class);
		}
		public UnaryContext unary(int i) {
			return getRuleContext(UnaryContext.class,i);
		}
		public List<TerminalNode> STAR() { return getTokens(CppSubsetParser.STAR); }
		public TerminalNode STAR(int i) {
			return getToken(CppSubsetParser.STAR, i);
		}
		public List<TerminalNode> SLASH() { return getTokens(CppSubsetParser.SLASH); }
		public TerminalNode SLASH(int i) {
			return getToken(CppSubsetParser.SLASH, i);
		}
		public List<TerminalNode> PERCENT() { return getTokens(CppSubsetParser.PERCENT); }
		public TerminalNode PERCENT(int i) {
			return getToken(CppSubsetParser.PERCENT, i);
		}
		public MulDivModContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mulDivMod; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterMulDivMod(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitMulDivMod(this);
		}
	}

	public final MulDivModContext mulDivMod() throws RecognitionException {
		MulDivModContext _localctx = new MulDivModContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_mulDivMod);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(250);
			unary();
			setState(255);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 15032385536L) != 0)) {
				{
				{
				setState(251);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 15032385536L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(252);
				unary();
				}
				}
				setState(257);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnaryContext extends ParserRuleContext {
		public UnaryContext unary() {
			return getRuleContext(UnaryContext.class,0);
		}
		public TerminalNode PLUS() { return getToken(CppSubsetParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(CppSubsetParser.MINUS, 0); }
		public TerminalNode NOT() { return getToken(CppSubsetParser.NOT, 0); }
		public PostfixContext postfix() {
			return getRuleContext(PostfixContext.class,0);
		}
		public UnaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterUnary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitUnary(this);
		}
	}

	public final UnaryContext unary() throws RecognitionException {
		UnaryContext _localctx = new UnaryContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_unary);
		int _la;
		try {
			setState(261);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
			case PLUS:
			case MINUS:
				enterOuterAlt(_localctx, 1);
				{
				setState(258);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1644167168L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(259);
				unary();
				}
				break;
			case TRUE:
			case FALSE:
			case IDENT:
			case INT_LIT:
			case CHAR_LIT:
			case STRING_LIT:
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(260);
				postfix();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PostfixContext extends ParserRuleContext {
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public List<PostfixSuffixContext> postfixSuffix() {
			return getRuleContexts(PostfixSuffixContext.class);
		}
		public PostfixSuffixContext postfixSuffix(int i) {
			return getRuleContext(PostfixSuffixContext.class,i);
		}
		public PostfixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postfix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterPostfix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitPostfix(this);
		}
	}

	public final PostfixContext postfix() throws RecognitionException {
		PostfixContext _localctx = new PostfixContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_postfix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(263);
			primary();
			setState(267);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT || _la==LPAREN) {
				{
				{
				setState(264);
				postfixSuffix();
				}
				}
				setState(269);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PostfixSuffixContext extends ParserRuleContext {
		public TerminalNode DOT() { return getToken(CppSubsetParser.DOT, 0); }
		public TerminalNode IDENT() { return getToken(CppSubsetParser.IDENT, 0); }
		public TerminalNode LPAREN() { return getToken(CppSubsetParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(CppSubsetParser.RPAREN, 0); }
		public ArgListContext argList() {
			return getRuleContext(ArgListContext.class,0);
		}
		public PostfixSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postfixSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterPostfixSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitPostfixSuffix(this);
		}
	}

	public final PostfixSuffixContext postfixSuffix() throws RecognitionException {
		PostfixSuffixContext _localctx = new PostfixSuffixContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_postfixSuffix);
		int _la;
		try {
			setState(284);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DOT:
				enterOuterAlt(_localctx, 1);
				{
				setState(270);
				match(DOT);
				setState(271);
				match(IDENT);
				setState(277);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
				case 1:
					{
					setState(272);
					match(LPAREN);
					setState(274);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 551400497152L) != 0)) {
						{
						setState(273);
						argList();
						}
					}

					setState(276);
					match(RPAREN);
					}
					break;
				}
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(279);
				match(LPAREN);
				setState(281);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 551400497152L) != 0)) {
					{
					setState(280);
					argList();
					}
				}

				setState(283);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(CppSubsetParser.IDENT, 0); }
		public TerminalNode INT_LIT() { return getToken(CppSubsetParser.INT_LIT, 0); }
		public TerminalNode TRUE() { return getToken(CppSubsetParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(CppSubsetParser.FALSE, 0); }
		public TerminalNode CHAR_LIT() { return getToken(CppSubsetParser.CHAR_LIT, 0); }
		public TerminalNode STRING_LIT() { return getToken(CppSubsetParser.STRING_LIT, 0); }
		public TerminalNode LPAREN() { return getToken(CppSubsetParser.LPAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(CppSubsetParser.RPAREN, 0); }
		public PrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitPrimary(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_primary);
		try {
			setState(296);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(286);
				match(IDENT);
				}
				break;
			case INT_LIT:
				enterOuterAlt(_localctx, 2);
				{
				setState(287);
				match(INT_LIT);
				}
				break;
			case TRUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(288);
				match(TRUE);
				}
				break;
			case FALSE:
				enterOuterAlt(_localctx, 4);
				{
				setState(289);
				match(FALSE);
				}
				break;
			case CHAR_LIT:
				enterOuterAlt(_localctx, 5);
				{
				setState(290);
				match(CHAR_LIT);
				}
				break;
			case STRING_LIT:
				enterOuterAlt(_localctx, 6);
				{
				setState(291);
				match(STRING_LIT);
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 7);
				{
				setState(292);
				match(LPAREN);
				setState(293);
				expr();
				setState(294);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgListContext extends ParserRuleContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CppSubsetParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CppSubsetParser.COMMA, i);
		}
		public ArgListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).enterArgList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CppSubsetListener ) ((CppSubsetListener)listener).exitArgList(this);
		}
	}

	public final ArgListContext argList() throws RecognitionException {
		ArgListContext _localctx = new ArgListContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_argList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(298);
			expr();
			setState(303);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(299);
				match(COMMA);
				setState(300);
				expr();
				}
				}
				setState(305);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001.\u0133\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0001\u0000\u0005\u0000D\b\u0000"+
		"\n\u0000\f\u0000G\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0003\u0001M\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0003\u0002S\b\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0005\u0003[\b\u0003\n\u0003\f\u0003^\t\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0003\u0005h\b\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0005\u0005n\b\u0005\n\u0005\f\u0005q\t\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0003\u0006y\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\b\u0003\b\u0080\b\b\u0001\b\u0001\b\u0001\b\u0001\b\u0003\b\u0086"+
		"\b\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0003\t\u008e\b\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0003"+
		"\u000b\u0097\b\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0005\r\u009d\b\r"+
		"\n\r\f\r\u00a0\t\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u00aa\b\u000e\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u00b0\b\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011"+
		"\u00be\b\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0013\u0001\u0013\u0003\u0013\u00c8\b\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0003\u0015\u00d1\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016"+
		"\u00d6\b\u0016\n\u0016\f\u0016\u00d9\t\u0016\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0005\u0017\u00de\b\u0017\n\u0017\f\u0017\u00e1\t\u0017\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0005\u0018\u00e6\b\u0018\n\u0018\f\u0018\u00e9"+
		"\t\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u00ee\b\u0019"+
		"\n\u0019\f\u0019\u00f1\t\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0005"+
		"\u001a\u00f6\b\u001a\n\u001a\f\u001a\u00f9\t\u001a\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0005\u001b\u00fe\b\u001b\n\u001b\f\u001b\u0101\t\u001b\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u0106\b\u001c\u0001\u001d\u0001"+
		"\u001d\u0005\u001d\u010a\b\u001d\n\u001d\f\u001d\u010d\t\u001d\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u0113\b\u001e\u0001\u001e"+
		"\u0003\u001e\u0116\b\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u011a\b"+
		"\u001e\u0001\u001e\u0003\u001e\u011d\b\u001e\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0003\u001f\u0129\b\u001f\u0001 \u0001 \u0001 \u0005"+
		" \u012e\b \n \f \u0131\t \u0001 \u0000\u0000!\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,."+
		"02468:<>@\u0000\u0006\u0002\u0000\u0001\u0005\u000f\u000f\u0001\u0000"+
		"\u0015\u0016\u0002\u0000\u0017\u0018\u001b\u001c\u0001\u0000\u001d\u001e"+
		"\u0001\u0000\u001f!\u0002\u0000\u0019\u0019\u001d\u001e\u013a\u0000E\u0001"+
		"\u0000\u0000\u0000\u0002L\u0001\u0000\u0000\u0000\u0004N\u0001\u0000\u0000"+
		"\u0000\u0006W\u0001\u0000\u0000\u0000\b_\u0001\u0000\u0000\u0000\nb\u0001"+
		"\u0000\u0000\u0000\fx\u0001\u0000\u0000\u0000\u000ez\u0001\u0000\u0000"+
		"\u0000\u0010\u007f\u0001\u0000\u0000\u0000\u0012\u008a\u0001\u0000\u0000"+
		"\u0000\u0014\u0092\u0001\u0000\u0000\u0000\u0016\u0094\u0001\u0000\u0000"+
		"\u0000\u0018\u0098\u0001\u0000\u0000\u0000\u001a\u009a\u0001\u0000\u0000"+
		"\u0000\u001c\u00a9\u0001\u0000\u0000\u0000\u001e\u00ab\u0001\u0000\u0000"+
		"\u0000 \u00b3\u0001\u0000\u0000\u0000\"\u00b6\u0001\u0000\u0000\u0000"+
		"$\u00bf\u0001\u0000\u0000\u0000&\u00c5\u0001\u0000\u0000\u0000(\u00cb"+
		"\u0001\u0000\u0000\u0000*\u00cd\u0001\u0000\u0000\u0000,\u00d2\u0001\u0000"+
		"\u0000\u0000.\u00da\u0001\u0000\u0000\u00000\u00e2\u0001\u0000\u0000\u0000"+
		"2\u00ea\u0001\u0000\u0000\u00004\u00f2\u0001\u0000\u0000\u00006\u00fa"+
		"\u0001\u0000\u0000\u00008\u0105\u0001\u0000\u0000\u0000:\u0107\u0001\u0000"+
		"\u0000\u0000<\u011c\u0001\u0000\u0000\u0000>\u0128\u0001\u0000\u0000\u0000"+
		"@\u012a\u0001\u0000\u0000\u0000BD\u0003\u0002\u0001\u0000CB\u0001\u0000"+
		"\u0000\u0000DG\u0001\u0000\u0000\u0000EC\u0001\u0000\u0000\u0000EF\u0001"+
		"\u0000\u0000\u0000FH\u0001\u0000\u0000\u0000GE\u0001\u0000\u0000\u0000"+
		"HI\u0005\u0000\u0000\u0001I\u0001\u0001\u0000\u0000\u0000JM\u0003\u0004"+
		"\u0002\u0000KM\u0003\n\u0005\u0000LJ\u0001\u0000\u0000\u0000LK\u0001\u0000"+
		"\u0000\u0000M\u0003\u0001\u0000\u0000\u0000NO\u0003\u0018\f\u0000OP\u0005"+
		"\u000f\u0000\u0000PR\u0005\'\u0000\u0000QS\u0003\u0006\u0003\u0000RQ\u0001"+
		"\u0000\u0000\u0000RS\u0001\u0000\u0000\u0000ST\u0001\u0000\u0000\u0000"+
		"TU\u0005(\u0000\u0000UV\u0003\u001a\r\u0000V\u0005\u0001\u0000\u0000\u0000"+
		"W\\\u0003\b\u0004\u0000XY\u0005$\u0000\u0000Y[\u0003\b\u0004\u0000ZX\u0001"+
		"\u0000\u0000\u0000[^\u0001\u0000\u0000\u0000\\Z\u0001\u0000\u0000\u0000"+
		"\\]\u0001\u0000\u0000\u0000]\u0007\u0001\u0000\u0000\u0000^\\\u0001\u0000"+
		"\u0000\u0000_`\u0003\u0016\u000b\u0000`a\u0005\u000f\u0000\u0000a\t\u0001"+
		"\u0000\u0000\u0000bc\u0005\u0006\u0000\u0000cg\u0005\u000f\u0000\u0000"+
		"de\u0005&\u0000\u0000ef\u0005\u0007\u0000\u0000fh\u0005\u000f\u0000\u0000"+
		"gd\u0001\u0000\u0000\u0000gh\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000"+
		"\u0000ij\u0005)\u0000\u0000jk\u0005\u0007\u0000\u0000ko\u0005&\u0000\u0000"+
		"ln\u0003\f\u0006\u0000ml\u0001\u0000\u0000\u0000nq\u0001\u0000\u0000\u0000"+
		"om\u0001\u0000\u0000\u0000op\u0001\u0000\u0000\u0000pr\u0001\u0000\u0000"+
		"\u0000qo\u0001\u0000\u0000\u0000rs\u0005*\u0000\u0000st\u0005%\u0000\u0000"+
		"t\u000b\u0001\u0000\u0000\u0000uy\u0003\u000e\u0007\u0000vy\u0003\u0010"+
		"\b\u0000wy\u0003\u0012\t\u0000xu\u0001\u0000\u0000\u0000xv\u0001\u0000"+
		"\u0000\u0000xw\u0001\u0000\u0000\u0000y\r\u0001\u0000\u0000\u0000z{\u0003"+
		"\u0014\n\u0000{|\u0005\u000f\u0000\u0000|}\u0005%\u0000\u0000}\u000f\u0001"+
		"\u0000\u0000\u0000~\u0080\u0005\b\u0000\u0000\u007f~\u0001\u0000\u0000"+
		"\u0000\u007f\u0080\u0001\u0000\u0000\u0000\u0080\u0081\u0001\u0000\u0000"+
		"\u0000\u0081\u0082\u0003\u0018\f\u0000\u0082\u0083\u0005\u000f\u0000\u0000"+
		"\u0083\u0085\u0005\'\u0000\u0000\u0084\u0086\u0003\u0006\u0003\u0000\u0085"+
		"\u0084\u0001\u0000\u0000\u0000\u0085\u0086\u0001\u0000\u0000\u0000\u0086"+
		"\u0087\u0001\u0000\u0000\u0000\u0087\u0088\u0005(\u0000\u0000\u0088\u0089"+
		"\u0003\u001a\r\u0000\u0089\u0011\u0001\u0000\u0000\u0000\u008a\u008b\u0005"+
		"\u000f\u0000\u0000\u008b\u008d\u0005\'\u0000\u0000\u008c\u008e\u0003\u0006"+
		"\u0003\u0000\u008d\u008c\u0001\u0000\u0000\u0000\u008d\u008e\u0001\u0000"+
		"\u0000\u0000\u008e\u008f\u0001\u0000\u0000\u0000\u008f\u0090\u0005(\u0000"+
		"\u0000\u0090\u0091\u0003\u001a\r\u0000\u0091\u0013\u0001\u0000\u0000\u0000"+
		"\u0092\u0093\u0007\u0000\u0000\u0000\u0093\u0015\u0001\u0000\u0000\u0000"+
		"\u0094\u0096\u0003\u0014\n\u0000\u0095\u0097\u0005\"\u0000\u0000\u0096"+
		"\u0095\u0001\u0000\u0000\u0000\u0096\u0097\u0001\u0000\u0000\u0000\u0097"+
		"\u0017\u0001\u0000\u0000\u0000\u0098\u0099\u0003\u0014\n\u0000\u0099\u0019"+
		"\u0001\u0000\u0000\u0000\u009a\u009e\u0005)\u0000\u0000\u009b\u009d\u0003"+
		"\u001c\u000e\u0000\u009c\u009b\u0001\u0000\u0000\u0000\u009d\u00a0\u0001"+
		"\u0000\u0000\u0000\u009e\u009c\u0001\u0000\u0000\u0000\u009e\u009f\u0001"+
		"\u0000\u0000\u0000\u009f\u00a1\u0001\u0000\u0000\u0000\u00a0\u009e\u0001"+
		"\u0000\u0000\u0000\u00a1\u00a2\u0005*\u0000\u0000\u00a2\u001b\u0001\u0000"+
		"\u0000\u0000\u00a3\u00aa\u0003\u001e\u000f\u0000\u00a4\u00aa\u0003 \u0010"+
		"\u0000\u00a5\u00aa\u0003\"\u0011\u0000\u00a6\u00aa\u0003$\u0012\u0000"+
		"\u00a7\u00aa\u0003&\u0013\u0000\u00a8\u00aa\u0003\u001a\r\u0000\u00a9"+
		"\u00a3\u0001\u0000\u0000\u0000\u00a9\u00a4\u0001\u0000\u0000\u0000\u00a9"+
		"\u00a5\u0001\u0000\u0000\u0000\u00a9\u00a6\u0001\u0000\u0000\u0000\u00a9"+
		"\u00a7\u0001\u0000\u0000\u0000\u00a9\u00a8\u0001\u0000\u0000\u0000\u00aa"+
		"\u001d\u0001\u0000\u0000\u0000\u00ab\u00ac\u0003\u0016\u000b\u0000\u00ac"+
		"\u00af\u0005\u000f\u0000\u0000\u00ad\u00ae\u0005\u001a\u0000\u0000\u00ae"+
		"\u00b0\u0003(\u0014\u0000\u00af\u00ad\u0001\u0000\u0000\u0000\u00af\u00b0"+
		"\u0001\u0000\u0000\u0000\u00b0\u00b1\u0001\u0000\u0000\u0000\u00b1\u00b2"+
		"\u0005%\u0000\u0000\u00b2\u001f\u0001\u0000\u0000\u0000\u00b3\u00b4\u0003"+
		"(\u0014\u0000\u00b4\u00b5\u0005%\u0000\u0000\u00b5!\u0001\u0000\u0000"+
		"\u0000\u00b6\u00b7\u0005\t\u0000\u0000\u00b7\u00b8\u0005\'\u0000\u0000"+
		"\u00b8\u00b9\u0003(\u0014\u0000\u00b9\u00ba\u0005(\u0000\u0000\u00ba\u00bd"+
		"\u0003\u001c\u000e\u0000\u00bb\u00bc\u0005\n\u0000\u0000\u00bc\u00be\u0003"+
		"\u001c\u000e\u0000\u00bd\u00bb\u0001\u0000\u0000\u0000\u00bd\u00be\u0001"+
		"\u0000\u0000\u0000\u00be#\u0001\u0000\u0000\u0000\u00bf\u00c0\u0005\u000b"+
		"\u0000\u0000\u00c0\u00c1\u0005\'\u0000\u0000\u00c1\u00c2\u0003(\u0014"+
		"\u0000\u00c2\u00c3\u0005(\u0000\u0000\u00c3\u00c4\u0003\u001c\u000e\u0000"+
		"\u00c4%\u0001\u0000\u0000\u0000\u00c5\u00c7\u0005\f\u0000\u0000\u00c6"+
		"\u00c8\u0003(\u0014\u0000\u00c7\u00c6\u0001\u0000\u0000\u0000\u00c7\u00c8"+
		"\u0001\u0000\u0000\u0000\u00c8\u00c9\u0001\u0000\u0000\u0000\u00c9\u00ca"+
		"\u0005%\u0000\u0000\u00ca\'\u0001\u0000\u0000\u0000\u00cb\u00cc\u0003"+
		"*\u0015\u0000\u00cc)\u0001\u0000\u0000\u0000\u00cd\u00d0\u0003,\u0016"+
		"\u0000\u00ce\u00cf\u0005\u001a\u0000\u0000\u00cf\u00d1\u0003*\u0015\u0000"+
		"\u00d0\u00ce\u0001\u0000\u0000\u0000\u00d0\u00d1\u0001\u0000\u0000\u0000"+
		"\u00d1+\u0001\u0000\u0000\u0000\u00d2\u00d7\u0003.\u0017\u0000\u00d3\u00d4"+
		"\u0005\u0014\u0000\u0000\u00d4\u00d6\u0003.\u0017\u0000\u00d5\u00d3\u0001"+
		"\u0000\u0000\u0000\u00d6\u00d9\u0001\u0000\u0000\u0000\u00d7\u00d5\u0001"+
		"\u0000\u0000\u0000\u00d7\u00d8\u0001\u0000\u0000\u0000\u00d8-\u0001\u0000"+
		"\u0000\u0000\u00d9\u00d7\u0001\u0000\u0000\u0000\u00da\u00df\u00030\u0018"+
		"\u0000\u00db\u00dc\u0005\u0013\u0000\u0000\u00dc\u00de\u00030\u0018\u0000"+
		"\u00dd\u00db\u0001\u0000\u0000\u0000\u00de\u00e1\u0001\u0000\u0000\u0000"+
		"\u00df\u00dd\u0001\u0000\u0000\u0000\u00df\u00e0\u0001\u0000\u0000\u0000"+
		"\u00e0/\u0001\u0000\u0000\u0000\u00e1\u00df\u0001\u0000\u0000\u0000\u00e2"+
		"\u00e7\u00032\u0019\u0000\u00e3\u00e4\u0007\u0001\u0000\u0000\u00e4\u00e6"+
		"\u00032\u0019\u0000\u00e5\u00e3\u0001\u0000\u0000\u0000\u00e6\u00e9\u0001"+
		"\u0000\u0000\u0000\u00e7\u00e5\u0001\u0000\u0000\u0000\u00e7\u00e8\u0001"+
		"\u0000\u0000\u0000\u00e81\u0001\u0000\u0000\u0000\u00e9\u00e7\u0001\u0000"+
		"\u0000\u0000\u00ea\u00ef\u00034\u001a\u0000\u00eb\u00ec\u0007\u0002\u0000"+
		"\u0000\u00ec\u00ee\u00034\u001a\u0000\u00ed\u00eb\u0001\u0000\u0000\u0000"+
		"\u00ee\u00f1\u0001\u0000\u0000\u0000\u00ef\u00ed\u0001\u0000\u0000\u0000"+
		"\u00ef\u00f0\u0001\u0000\u0000\u0000\u00f03\u0001\u0000\u0000\u0000\u00f1"+
		"\u00ef\u0001\u0000\u0000\u0000\u00f2\u00f7\u00036\u001b\u0000\u00f3\u00f4"+
		"\u0007\u0003\u0000\u0000\u00f4\u00f6\u00036\u001b\u0000\u00f5\u00f3\u0001"+
		"\u0000\u0000\u0000\u00f6\u00f9\u0001\u0000\u0000\u0000\u00f7\u00f5\u0001"+
		"\u0000\u0000\u0000\u00f7\u00f8\u0001\u0000\u0000\u0000\u00f85\u0001\u0000"+
		"\u0000\u0000\u00f9\u00f7\u0001\u0000\u0000\u0000\u00fa\u00ff\u00038\u001c"+
		"\u0000\u00fb\u00fc\u0007\u0004\u0000\u0000\u00fc\u00fe\u00038\u001c\u0000"+
		"\u00fd\u00fb\u0001\u0000\u0000\u0000\u00fe\u0101\u0001\u0000\u0000\u0000"+
		"\u00ff\u00fd\u0001\u0000\u0000\u0000\u00ff\u0100\u0001\u0000\u0000\u0000"+
		"\u01007\u0001\u0000\u0000\u0000\u0101\u00ff\u0001\u0000\u0000\u0000\u0102"+
		"\u0103\u0007\u0005\u0000\u0000\u0103\u0106\u00038\u001c\u0000\u0104\u0106"+
		"\u0003:\u001d\u0000\u0105\u0102\u0001\u0000\u0000\u0000\u0105\u0104\u0001"+
		"\u0000\u0000\u0000\u01069\u0001\u0000\u0000\u0000\u0107\u010b\u0003>\u001f"+
		"\u0000\u0108\u010a\u0003<\u001e\u0000\u0109\u0108\u0001\u0000\u0000\u0000"+
		"\u010a\u010d\u0001\u0000\u0000\u0000\u010b\u0109\u0001\u0000\u0000\u0000"+
		"\u010b\u010c\u0001\u0000\u0000\u0000\u010c;\u0001\u0000\u0000\u0000\u010d"+
		"\u010b\u0001\u0000\u0000\u0000\u010e\u010f\u0005#\u0000\u0000\u010f\u0115"+
		"\u0005\u000f\u0000\u0000\u0110\u0112\u0005\'\u0000\u0000\u0111\u0113\u0003"+
		"@ \u0000\u0112\u0111\u0001\u0000\u0000\u0000\u0112\u0113\u0001\u0000\u0000"+
		"\u0000\u0113\u0114\u0001\u0000\u0000\u0000\u0114\u0116\u0005(\u0000\u0000"+
		"\u0115\u0110\u0001\u0000\u0000\u0000\u0115\u0116\u0001\u0000\u0000\u0000"+
		"\u0116\u011d\u0001\u0000\u0000\u0000\u0117\u0119\u0005\'\u0000\u0000\u0118"+
		"\u011a\u0003@ \u0000\u0119\u0118\u0001\u0000\u0000\u0000\u0119\u011a\u0001"+
		"\u0000\u0000\u0000\u011a\u011b\u0001\u0000\u0000\u0000\u011b\u011d\u0005"+
		"(\u0000\u0000\u011c\u010e\u0001\u0000\u0000\u0000\u011c\u0117\u0001\u0000"+
		"\u0000\u0000\u011d=\u0001\u0000\u0000\u0000\u011e\u0129\u0005\u000f\u0000"+
		"\u0000\u011f\u0129\u0005\u0010\u0000\u0000\u0120\u0129\u0005\r\u0000\u0000"+
		"\u0121\u0129\u0005\u000e\u0000\u0000\u0122\u0129\u0005\u0011\u0000\u0000"+
		"\u0123\u0129\u0005\u0012\u0000\u0000\u0124\u0125\u0005\'\u0000\u0000\u0125"+
		"\u0126\u0003(\u0014\u0000\u0126\u0127\u0005(\u0000\u0000\u0127\u0129\u0001"+
		"\u0000\u0000\u0000\u0128\u011e\u0001\u0000\u0000\u0000\u0128\u011f\u0001"+
		"\u0000\u0000\u0000\u0128\u0120\u0001\u0000\u0000\u0000\u0128\u0121\u0001"+
		"\u0000\u0000\u0000\u0128\u0122\u0001\u0000\u0000\u0000\u0128\u0123\u0001"+
		"\u0000\u0000\u0000\u0128\u0124\u0001\u0000\u0000\u0000\u0129?\u0001\u0000"+
		"\u0000\u0000\u012a\u012f\u0003(\u0014\u0000\u012b\u012c\u0005$\u0000\u0000"+
		"\u012c\u012e\u0003(\u0014\u0000\u012d\u012b\u0001\u0000\u0000\u0000\u012e"+
		"\u0131\u0001\u0000\u0000\u0000\u012f\u012d\u0001\u0000\u0000\u0000\u012f"+
		"\u0130\u0001\u0000\u0000\u0000\u0130A\u0001\u0000\u0000\u0000\u0131\u012f"+
		"\u0001\u0000\u0000\u0000\u001fELR\\gox\u007f\u0085\u008d\u0096\u009e\u00a9"+
		"\u00af\u00bd\u00c7\u00d0\u00d7\u00df\u00e7\u00ef\u00f7\u00ff\u0105\u010b"+
		"\u0112\u0115\u0119\u011c\u0128\u012f";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}