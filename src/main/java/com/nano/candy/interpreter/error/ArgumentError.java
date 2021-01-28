package com.nano.candy.interpreter.error;

public class ArgumentError extends CandyRuntimeError{

	public static void checkArguments(int expected, int actual){
		checkArguments(expected, actual, "callable object");
	}
	
	public static void checkArguments(int expected, int actual, String callableName) {
		if (expected != actual) {
			throw new ArgumentError(expected, actual, callableName);
		}
	}
	
	public ArgumentError(int expected, int actual, String callableName) {
		this("The %s takes %d arguments, but %d were given.",
			callableName, expected, actual
		);
	}    
	
	public ArgumentError(String msg, Object... args) {
		super(String.format(msg, args));
	}
}
