package com.nano.candy.interpreter.i2.runtime;

public class VMExitException extends RuntimeException {
	public int code;
	public VMExitException(int code) {
		this.code = code;
	}    
}
