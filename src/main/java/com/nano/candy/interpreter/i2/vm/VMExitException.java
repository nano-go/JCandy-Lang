package com.nano.candy.interpreter.i2.vm;

public class VMExitException extends RuntimeException {
	int code;
	public VMExitException(int code) {
		this.code = code;
	}    
}
