package com.nano.candy.interpreter.i2.vm;
import com.nano.candy.interpreter.i2.builtin.type.error.ErrorObj;

public class CarrierErrorException extends RuntimeException {
	private ErrorObj errorObj;

	public CarrierErrorException(ErrorObj errorObj) {
		this.errorObj = errorObj;
	}

	public ErrorObj getErrorObj() {
		return errorObj;
	}
}
