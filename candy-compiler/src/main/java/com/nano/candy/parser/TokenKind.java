package com.nano.candy.parser;

import com.nano.candy.parser.TokenKind;
import java.util.HashMap;

public enum TokenKind {
	
	ASSIGN("="),
	LEFT_SHIFT_ASSIGN("<<="),
	RIGHT_SHIFT_ASSIGN(">>="),
	PLUS_ASSIGN("+="),
	MINUS_ASSIGN("-="),
	STAR_ASSIGN("*="),
	DIV_ASSIGN("/="),
	MOD_ASSIGN("%="),
	
	LPAREN("("),
	RPAREN(")"),
	LBRACKET("["),
	RBRACKET("]"),
	LBRACE("{"),
	RBRACE("}"),
	QUESITION("?"),
	COLON(":"),
	COMMA(","),
	DOT("."),
	ARROW("->"),
	BIT_OR("|"), // is not binary operator yet.
	SEMI(";"),
	
	NOT("!"),
	PLUS("+"),
	MINUS("-"),
	STAR("*"),
	DIV("/"),
	MOD("%"),
	DOT_DOT(".."),
	LOGICAL_AND("and"),
	LOGICAL_OR("or"),
	LEFT_SHIFT("<<"),
	RIGHT_SHIFT(">>"),
	IS("is"),
	EQUAL("=="),
	NOT_EQUAL("!="),
	GT(">"),
	GTEQ(">="),
	LT("<"),
	LTEQ("<="),

	VAR("var"),
	PUBLIC("pub"),
	PRIVATE("pri"),
	READER("reader"),
	WRITER("writer"),
	ASSERT("assert"),
	IF("if"),
	ELSE("else"),
	TRY("try"),
	INTERCEPT("intercept"),
	RAISE("raise"),
	WHILE("while"),
	FOR("for"),
	IN("in"),
	BREAK("break"),
	CONTINUE("continue"),
	RETURN("return"),
	FUN("fun"),
	LAMBDA("lambda"),
	CLASS("class"),
	STATIC("static"),
	IMPORT("import"),
	FINALLY("finally"),
	AS("as"),
	TRUE("true"),
	FALSE("false"),
	NULL("null"),
	THIS("this"),
	SUPER("super"),
	
	EOF,
	IDENTIFIER,
	AT_IDENTIFIER,
	DOUBLE,
	INTEGER,
	STRING,
	INTERPOLATION;
	
	private static final HashMap<String, TokenKind> KEYWORD_KINDS = 
		new HashMap<String, TokenKind>();
	static {
		for (int i = VAR.ordinal(); i <= SUPER.ordinal(); i ++) {
			TokenKind keyword = TokenKind.values()[i];
			KEYWORD_KINDS.put(keyword.literal, keyword);
		}
		KEYWORD_KINDS.put(LOGICAL_AND.literal, LOGICAL_AND);
		KEYWORD_KINDS.put(LOGICAL_OR.literal, LOGICAL_OR);
		KEYWORD_KINDS.put(IS.literal, IS);
	}

	public static TokenKind lookupKind(String identifier) {
		TokenKind kind = KEYWORD_KINDS.get(identifier);
		return kind == null ? TokenKind.IDENTIFIER : kind;
	}
	
	public static boolean isLogicalOperator(TokenKind operator) {
		switch (operator) {
			case LOGICAL_OR:
			case LOGICAL_AND:
			case NOT:
				return true;
		}
		return false;
	}
	
	public static int assignOperatorStart() {
		return ASSIGN.ordinal();
	}
	
	public static int assignOperatorEnd() {
		return MOD_ASSIGN.ordinal();
	}
	
	public static int binaryOperatorOrdinalStart() {
		return PLUS.ordinal();
	}
	
	public static int binaryOperatorOrdinalEnd() {
		return LTEQ.ordinal();
	}
	
	public static int unaryOperatorOrdinalStart() {
		return NOT.ordinal();
	}

	public static int unaryOperatorOrdinalEnd() {
		return MINUS.ordinal();
	}
	
	public static boolean isAssignOperator(TokenKind kind) {
		return kind.ordinal() >= assignOperatorStart() && 
		       kind.ordinal() <= assignOperatorEnd();
	}
	
	public static boolean isBinaryOperator(TokenKind kind) {
		return kind.ordinal() >= binaryOperatorOrdinalStart() && 
		       kind.ordinal() <= binaryOperatorOrdinalEnd();
	}
	
	public static boolean isUnaryOperator(TokenKind kind) {
		return kind.ordinal() >= unaryOperatorOrdinalStart() && 
		       kind.ordinal() <= unaryOperatorOrdinalEnd();
	}
	
	public static int precedence(TokenKind kind) {
		switch (kind) {
			case DOT_DOT:
				return 1;
			case LOGICAL_OR: 
				return 2;
			case LOGICAL_AND: 
				return 3;
			case EQUAL: 
			case NOT_EQUAL: 
				return 4;
			case GT: case GTEQ: 
			case LT: case LTEQ: 
			case IS:
				return 5;
			case LEFT_SHIFT: case RIGHT_SHIFT:
				return 6;
			case PLUS: case MINUS: 
				return 7;
			case STAR: case DIV: case MOD: 
				return 8;
		}
		return 0;
	}

	protected final String literal;
	private TokenKind() {
		this("");
	}	
	private TokenKind(String literal){
		this.literal = literal;
	}
	
	public String getLiteral() {
		return literal;
	}
}
