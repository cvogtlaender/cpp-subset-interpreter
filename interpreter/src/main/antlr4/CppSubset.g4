grammar CppSubset;

program
    : topLevelDecl* EOF
    ;

topLevelDecl
    : functionDecl
    | classDecl
    ;

// Functions

// Return types may not be references (enforced in the semantic checker).
functionDecl
    : returnType IDENT '(' paramList? ')' block
    ;

paramList
    : param (',' param)*
    ;

// Parameters may be references: T& p
param
    : typeRef IDENT
    ;

// Classes

// Syntax: class Name { public: members... };
// Optional single public base class.
classDecl
    : 'class' IDENT (':' 'public' IDENT)? '{' 'public' ':' memberDecl* '}' ';'
    ;

memberDecl
    : fieldDecl
    | methodDecl
    | constructorDecl
    ;

// Fields may not be references (enforced in the semantic checker).
fieldDecl
    : baseType IDENT ';'
    ;

// Methods may be virtual. Return types may not be references.
methodDecl
    : 'virtual'? returnType IDENT '(' paramList? ')' block
    ;

// Constructor: no return type, name must match enclosing class (semantic check).
constructorDecl
    : IDENT '(' paramList? ')' block
    ;

// Types

// Base type: a primitive keyword or a user-defined class name.
baseType
    : 'bool'
    | 'int'
    | 'char'
    | 'string'
    | 'void'
    | IDENT
    ;

// A type that may carry a reference modifier '&'.
// Used in parameter and local-variable declarations.
typeRef
    : baseType '&'?
    ;

// Return types are never references; void is allowed.
returnType
    : baseType
    ;

// Statements

block
    : '{' statement* '}'
    ;

// ANTLR4's adaptive LL(*) resolves the varDecl / exprStmt ambiguity at runtime:
//   - A statement beginning with a type keyword is always a varDecl.
//   - A statement beginning with IDENT '&'? IDENT is a varDecl (class-typed local).
//   - Everything else is an exprStmt.
statement
    : varDecl
    | exprStmt
    | ifStmt
    | whileStmt
    | returnStmt
    | block
    ;

// Local variable declaration.  References require an initialiser (semantic check).
varDecl
    : typeRef IDENT ('=' expr)? ';'
    ;

exprStmt
    : expr ';'
    ;

ifStmt
    : 'if' '(' expr ')' statement ('else' statement)?
    ;

whileStmt
    : 'while' '(' expr ')' statement
    ;

// 'return;' is allowed for void functions.
returnStmt
    : 'return' expr? ';'
    ;

// Expressions
// Precedence follows the C++ standard (lowest to highest, top to bottom).
// No bitwise operators, no increment/decrement, no compound assignments.

expr
    : assignment
    ;

// Right-associative.  The lvalue restriction is enforced in the semantic checker.
assignment
    : logicalOr ('=' assignment)?
    ;

logicalOr
    : logicalAnd ('||' logicalAnd)*
    ;

logicalAnd
    : equality ('&&' equality)*
    ;

// bool and string only support == and != (semantic check).
equality
    : relational (('==' | '!=') relational)*
    ;

// Comparisons are defined for int and char only (semantic check).
relational
    : addSub (('<' | '>' | '<=' | '>=') addSub)*
    ;

// Arithmetic is defined for int only (semantic check).
addSub
    : mulDivMod (('+' | '-') mulDivMod)*
    ;

mulDivMod
    : unary (('*' | '/' | '%') unary)*
    ;

// Unary +/- are int-only; ! is bool-only (semantic check).
unary
    : ('+' | '-' | '!') unary
    | postfix
    ;

// Postfix: member access and function/method calls.
// Chains are parsed left-to-right, e.g. a.b().c
postfix
    : primary postfixSuffix*
    ;

postfixSuffix
    : '.' IDENT ('(' argList? ')')?   // field access or method call
    | '(' argList? ')'                 // free-function call
    ;

primary
    : IDENT                            // variable or function name
    | INT_LIT                          // integer literal
    | 'true'                           // bool literal
    | 'false'                          // bool literal
    | CHAR_LIT                         // character literal
    | STRING_LIT                       // string literal
    | '(' expr ')'                     // parenthesised expression
    ;

argList
    : expr (',' expr)*
    ;

// Lexer
// Keywords must appear before IDENT so that the lexer gives them priority.

// Type keywords
BOOL   : 'bool'   ;
INT    : 'int'    ;
CHAR   : 'char'   ;
STRING : 'string' ;
VOID   : 'void'   ;

// Declaration keywords
CLASS   : 'class'   ;
PUBLIC  : 'public'  ;
VIRTUAL : 'virtual' ;

// Statement keywords
IF     : 'if'     ;
ELSE   : 'else'   ;
WHILE  : 'while'  ;
RETURN : 'return' ;

// Boolean literals (before IDENT to avoid being tokenised as identifiers)
TRUE  : 'true'  ;
FALSE : 'false' ;

// Identifiers
IDENT : [a-zA-Z_][a-zA-Z0-9_]* ;

// Integer literals
INT_LIT : [0-9]+ ;

// Character literal: single character or escape sequence inside single quotes
CHAR_LIT : '\'' (ESCAPE_SEQ | ~['\\\r\n]) '\'' ;

// String literal: zero or more characters or escape sequences inside double quotes
STRING_LIT : '"' (ESCAPE_SEQ | ~["\\\r\n])* '"' ;

// Supported escape sequences: \n \t \r \\ \' \" \0
fragment ESCAPE_SEQ : '\\' [ntr\\'\"0] ;

// Multi-character operators (must come before their single-character prefixes)
AND : '&&' ;
OR  : '||' ;
EQ  : '==' ;
NEQ : '!=' ;
LEQ : '<=' ;
GEQ : '>=' ;

// Single-character operators and punctuation
NOT    : '!'  ;
ASSIGN : '='  ;
LT     : '<'  ;
GT     : '>'  ;
PLUS   : '+'  ;
MINUS  : '-'  ;
STAR   : '*'  ;
SLASH  : '/'  ;
PERCENT: '%'  ;
REF    : '&'  ;
DOT    : '.'  ;
COMMA  : ','  ;
SEMI   : ';'  ;
COLON  : ':'  ;
LPAREN : '('  ;
RPAREN : ')'  ;
LBRACE : '{'  ;
RBRACE : '}'  ;

// Skipped input
WS            : [ \t\r\n]+  -> skip ;
LINE_COMMENT  : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
// Preprocessor lines are ignored (e.g. #include, #define)
PREPROCESSOR  : '#' ~[\r\n]* -> skip ;
