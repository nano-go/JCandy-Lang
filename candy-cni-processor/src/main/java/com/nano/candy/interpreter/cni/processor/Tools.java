package com.nano.candy.interpreter.cni.processor;

import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

public class Tools {

	public static void throwArgException(String msg, Object... args) {
		throw new IllegalArgumentException(String.format(msg, args));
	}
	
	/** Avoid format exception. */
	public static void throwArgException(String msg) {
		throw new IllegalArgumentException(msg);
	}
	
	public static boolean isPublicOrProtectedMethod(Element e) {
		Set<Modifier> modifiers = e.getModifiers();
		return e.getKind() == ElementKind.METHOD &&
			e.getEnclosingElement() != null &&
			e.getEnclosingElement().getKind() == ElementKind.CLASS &&
			!modifiers.contains(Modifier.STATIC) &&
			!modifiers.contains(Modifier.ABSTRACT) &&
			(modifiers.contains(Modifier.PUBLIC) ||
			 modifiers.contains(Modifier.PROTECTED));
	}
	
	public static boolean isPublicStaticMethod(Element e) {
		return e.getKind() == ElementKind.METHOD &&
			e.getEnclosingElement() != null &&
			e.getEnclosingElement().getKind() == ElementKind.CLASS &&
			e.getModifiers().contains(Modifier.STATIC) &&
			e.getModifiers().contains(Modifier.PUBLIC);
	}
	
	public static boolean isClass(Element e) {
		return e.getKind() == ElementKind.CLASS;
	}
	
	public static boolean isCandyIdentifier(String id) {
		if (id == null || id.length() <= 0) {
			return false;
		}
		if (!isCandyIdentifierStart(id.charAt(0))) {
			return false;
		}
		for (int i = 1; i < id.length(); i ++) {
			if (!isCandyIdentifier(id.charAt(i))) 
				return false;
		}
		return true;
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
}
