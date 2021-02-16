package com.nano.candy.parser;

import com.google.common.collect.ImmutableMap;
import com.nano.candy.parser.TokenKind;

public enum TokenKind {
	
	ASSIGN("="),
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
	COLON(":"),
	COMMA(","),
	DOT("."),
	ARROW("->"),
	SEMI(";"),
	
	NOT("!"),
	PLUS("+"),
	MINUS("-"),
	STAR("*"),
	DIV("/"),
	MOD("%"),
	LOGICAL_AND("and"),
	LOGICAL_OR("or"),
	IS("is"),
	EQUAL("=="),
	NOT_EQUAL("!="),
	GT(">"),
	GTEQ(">="),
	LT("<"),
	LTEQ("<="),

	VAR("var"),
	ASSERT("assert"),
	IF("if"),
	ELSE("else"),
	WHILE("while"),
	FOR("for"),
	IN("in"),
	BREAK("break"),
	CONTINUE("continue"),
	RETURN("return"),
	FUN("fun"),
	LAMBDA("lambda"),
	CLASS("class"),
	TRUE("true"),
	FALSE("false"),
	NULL("null"),
	THIS("this"),
	SUPER("super"),
	
	EOF,
	IDENTIFIER,
	ARRAY,
	DOUBLE,
	INTEGER,
	STRING;
	
	private static final ImmutableMap<String, TokenKind> KEYWORD_KINDS = 
		ImmutableMap.<String, TokenKind>builder()
			.put(TokenKind.VAR.literal, TokenKind.VAR)
			.put(TokenKind.ASSERT.literal, TokenKind.ASSERT)
			.put(TokenKind.LOGICAL_OR.literal, TokenKind.LOGICAL_OR)
			.put(TokenKind.LOGICAL_AND.literal, TokenKind.LOGICAL_AND)
			.put(TokenKind.IS.literal, TokenKind.IS)
			.put(TokenKind.FOR.literal, TokenKind.FOR)
			.put(TokenKind.IN.literal, TokenKind.IN)
			.put(TokenKind.WHILE.literal, TokenKind.WHILE)
			.put(TokenKind.CONTINUE.literal, TokenKind.CONTINUE)
			.put(TokenKind.BREAK.literal, TokenKind.BREAK)
			.put(TokenKind.TRUE.literal, TokenKind.TRUE)
			.put(TokenKind.FALSE.literal, TokenKind.FALSE)
			.put(TokenKind.NULL.literal, TokenKind.NULL)
			.put(TokenKind.IF.literal, TokenKind.IF)
			.put(TokenKind.ELSE.literal, TokenKind.ELSE)
			.put(TokenKind.FUN.literal, TokenKind.FUN)
			.put(TokenKind.RETURN.literal, TokenKind.RETURN)
			.put(TokenKind.LAMBDA.literal, TokenKind.LAMBDA)
			.put(TokenKind.CLASS.literal, TokenKind.CLASS)
			.put(TokenKind.THIS.literal, TokenKind.THIS)
			.put(TokenKind.SUPER.literal, TokenKind.SUPER)
			.build();

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
			case LOGICAL_OR: 
				return 1;
			case LOGICAL_AND: 
				return 2;
			case EQUAL: 
			case NOT_EQUAL: 
				return 3;
			case GT: case GTEQ: 
			case LT: case LTEQ: 
			case IS:
				return 4;
			case PLUS: case MINUS: 
				return 5;
			case STAR: case DIV: case MOD: 
				return 6;
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
