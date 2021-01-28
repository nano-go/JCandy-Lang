package com.nano.candy.utils;

public class Characters {
	
	public static final char EOF = '\0' ;
	
	public static boolean isWhitespace(char ch) {
		return Character.isWhitespace(ch) ;
	}
	
	public static boolean isCandyIdentifier(char c) {
		return isCandyIdentifierStart(c) || isDigit(c) ;
	}
	
	public static boolean isCandyIdentifierStart(char c) {
		return isLetter(c) || c == '_' ;
	}

	public static boolean isLetter(char c) {
		return 'a' <= toLowerIfLetter(c) && toLowerIfLetter(c) <= 'z' ;
	}
	
	public static boolean isDigit(char c) {
		return c >= '0' && c <= '9' ;
	}
	
	public static char toLowerIfLetter(char letter) {
		return (char)(('a' - 'A') | letter) ;
	}
}
