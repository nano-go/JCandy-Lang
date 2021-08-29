package com.nano.candy.interpreter.runtime;

public class VMExitException extends RuntimeException {
	public int code;
	public VMExitException(int code) {
		this.code = code;
	}    
}
