package com.nano.candy.interpreter.i2.error;

public class NativeError extends CandyRuntimeError {
	
	public NativeError(String format, Object... args) {
		super(format, args);
	}
	
	public NativeError(Throwable t) {
		this("[%s](%s)", t.getClass().getSimpleName(), t.getMessage());
	}
	    
}
