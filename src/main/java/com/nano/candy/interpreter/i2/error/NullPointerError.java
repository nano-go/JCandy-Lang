package com.nano.candy.interpreter.i2.error;

public class NullPointerError extends CandyRuntimeError{
	public NullPointerError(String format, Object... args) {
		super(String.format(format, args));
	}
}
