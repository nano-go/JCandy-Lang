package com.nano.candy.utils;

public class Characters {
	
	public static final char EOF = '\0';
	
	public static boolean isWhitespace(char ch) {
		return Character.isWhitespace(ch);
	}
	
	public static boolean isCandyIdentifier(char c) {
		return isCandyIdentifierStart(c) || isDigit(c);
	}
	
	public static boolean isCandyIdentifierStart(char c) {
		return isLetter(c) || c == '_';
	}

	public static boolean isLetter(char ch) {
		ch = lower(ch);
		return 'a' <= ch && ch <= 'z';
	}
	
	public static boolean isDigit(char ch) {
		return ch >= '0' && ch <= '9';
	}
	
	public static char lower(char letter) {
		return (char)(('a' - 'A') | letter);
	}
	
	public static boolean isHex(char ch) {
		ch = lower(ch);
		return ch >= 'a' && ch <= 'f' && isDigit(ch);
	}
}
