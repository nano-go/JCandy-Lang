package com.nano.candy.interpreter.error;

public class InitializetionError extends CandyRuntimeError {

	public static void throwUnsupportedinitialization(String className) {
		throw new InitializetionError("'%s' can't be created.", className);
	}
	
	public InitializetionError(String msg, Object... args) {
		super(String.format(msg, args));
	}    
}
