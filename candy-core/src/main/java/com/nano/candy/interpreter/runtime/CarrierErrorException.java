package com.nano.candy.interpreter.runtime;
import com.nano.candy.interpreter.builtin.type.error.ErrorObj;

public class CarrierErrorException extends RuntimeException {
	private ErrorObj errorObj;

	public CarrierErrorException(ErrorObj errorObj) {
		this.errorObj = errorObj;
	}

	public ErrorObj getErrorObj() {
		return errorObj;
	}
}
