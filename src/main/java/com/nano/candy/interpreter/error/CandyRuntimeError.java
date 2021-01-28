package com.nano.candy.interpreter.error;
import com.nano.candy.utils.Position;

public class CandyRuntimeError extends RuntimeException{
	
	public CandyRuntimeError(String msg, Object... args) {
		super(String.format(msg, args));
	}
}
