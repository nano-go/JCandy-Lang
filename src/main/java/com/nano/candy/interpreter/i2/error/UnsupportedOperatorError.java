package com.nano.candy.interpreter.i2.error;

public class UnsupportedOperatorError extends CandyRuntimeError {
	
	public UnsupportedOperatorError(String format, Object... args) {
		super(String.format(format, args));
	}
}
