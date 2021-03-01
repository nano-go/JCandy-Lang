package com.nano.candy.interpreter.i2.error;

public class CandyRuntimeError extends RuntimeException {
	
	public CandyRuntimeError(String msg) {
		super(msg);
	}
	
	public CandyRuntimeError(String format, Object... args) {
		super(String.format(format, args));
	}
}
