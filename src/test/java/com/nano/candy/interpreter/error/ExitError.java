package com.nano.candy.interpreter.error;

public class ExitError extends RuntimeException{
	int code ;

	public ExitError(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
    
}
