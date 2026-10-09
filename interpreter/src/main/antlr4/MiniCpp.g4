grammar MiniCpp;

program
    : declaration* EOF
    ;

replInput
    : (declaration | statement)* expr? EOF
    ;

declaration
    : functionDef
    | classDef
    ;

// TYPES

type
    : (baseType | Identifier) STAR*
    ;

baseType
    : INT
    | BOOL
    | CHAR
    | STRING
    | VOID
    ;

typeRef
    : type AMP
    ;

// CLASSES

classDef
    : CLASS Identifier
      (COLON PUBLIC Identifier)?
      LBRACE PUBLIC COLON memberDecl* RBRACE SEMI
    ;

memberDecl
    : fieldDecl
    | constructorDef
    | methodDef
    ;

fieldDecl
    : type Identifier SEMI
    ;

constructorDef
    : Identifier LPAREN paramList? RPAREN block
    ;

methodDef
    : VIRTUAL? type Identifier
      LPAREN paramList? RPAREN
      block
    ;

// FUNCTIONS

functionDef
    : type Identifier
      LPAREN paramList? RPAREN
      block
    ;

paramList
    : param (COMMA param)*
    ;

param
    : (type | typeRef) Identifier
    ;

// param : type Identifier ;

// STATEMENTS

block
    : LBRACE statement* RBRACE
    ;

statement
    : block
    | varDecl SEMI
    | ifStmt
    | whileStmt
    | returnStmt SEMI
    | deleteStmt SEMI
    | expr SEMI
    ;

varDecl
    : type Identifier
    | type Identifier LPAREN argList RPAREN
    | (type | typeRef) Identifier ASSIGN expr
    ;

// varDecl		
//    : type Identifier 
//      (ASSIGN expr)?

ifStmt
    : IF LPAREN expr RPAREN statement
      (ELSE statement)?
    ;

whileStmt
    : WHILE LPAREN expr RPAREN statement
    ;

returnStmt
    : RETURN expr?
    ;

deleteStmt
    : DELETE expr
    ;

// EXPRESSIONS

expr
    : assignmentExpr
    ;

assignmentExpr
    : logicalOrExpr
      (ASSIGN assignmentExpr)?
    ;

logicalOrExpr
    : logicalAndExpr
      (OROR logicalAndExpr)*
    ;

logicalAndExpr
    : equalityExpr
      (ANDAND equalityExpr)*
    ;

equalityExpr
    : relationalExpr
      ((EQEQ | NOTEQ) relationalExpr)*
    ;

relationalExpr
    : additiveExpr
      ((LT | LE | GT | GE) additiveExpr)*
    ;

additiveExpr
    : multiplicativeExpr
      ((PLUS | MINUS) multiplicativeExpr)*
    ;

multiplicativeExpr
    : unaryExpr
      ((STAR | SLASH | PERCENT) unaryExpr)*
    ;

unaryExpr
    : (NOT | PLUS | MINUS | STAR | AMP) unaryExpr
    | newExpr
    | postfixExpr
    ;

newExpr
    : NEW (baseType | Identifier) (LPAREN argList? RPAREN)?
    ;

// POSTFIX EXPRESSIONS

postfixExpr
    : primaryExpr postfixPart*
    ;

postfixPart
    : LPAREN argList? RPAREN
    | DOT Identifier
    | ARROW Identifier
    ;

primaryExpr
    : literal
    | Identifier
    | LPAREN expr RPAREN
    ;

argList
    : expr (COMMA expr)*
    ;

// LITERALS

literal
    : IntLiteral
    | BoolLiteral
    | CharLiteral
    | StringLiteral
    | NULLPTR
    ;

// KEYWORDS

INT         : 'int';
BOOL        : 'bool';
CHAR        : 'char';
STRING      : 'string';
VOID        : 'void';

CLASS       : 'class';
PUBLIC      : 'public';
VIRTUAL     : 'virtual';

IF          : 'if';
ELSE        : 'else';
WHILE       : 'while';
RETURN      : 'return';

NEW         : 'new';
DELETE      : 'delete';
NULLPTR     : 'nullptr';

// OPERATORS / SYMBOLS

PLUS        : '+';
MINUS       : '-';
STAR        : '*';
SLASH       : '/';
PERCENT     : '%';

ASSIGN      : '=';

EQEQ        : '==';
NOTEQ       : '!=';

LT          : '<';
LE          : '<=';
GT          : '>';
GE          : '>=';

ANDAND      : '&&';
OROR        : '||';
NOT         : '!';

AMP         : '&';

DOT         : '.';
ARROW       : '->';

LPAREN      : '(';
RPAREN      : ')';

LBRACE      : '{';
RBRACE      : '}';

COMMA       : ',';
SEMI        : ';';
COLON       : ':';

// LITERALS

BoolLiteral
    : 'true'
    | 'false'
    ;

IntLiteral
    : [0-9]+
    ;

CharLiteral
    : '\'' (EscapeSequence | ~['\\]) '\''
    ;

StringLiteral
    : '"' (EscapeSequence | ~["\\])* '"'
    ;

fragment EscapeSequence
    : '\\' [btnr"'\\0]
    ;

// IDENTIFIER

Identifier
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

// COMMENTS / WHITESPACE

WS
    : [ \t\r\n]+ -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;

PREPROCESSOR
    : '#' ~[\r\n]* -> skip
    ;