package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;

@BuiltinClass("NativeError")
public class NativeError extends ErrorObj {
	public static final CandyClass NATIVE_ERROR_CLASS = 
		BuiltinClassFactory.generate(NativeError.class, ERROR_CLASS);
	
	public NativeError() {
		super(NATIVE_ERROR_CLASS);
	}
	
	public NativeError(String msg) {
		super(NATIVE_ERROR_CLASS, msg);
	}
	
	public NativeError(Throwable throwable) {
		this(String.format(
			"A java error occurs.\n    %s: %s", 
			throwable.getClass().getSimpleName(), 
			throwable.getMessage()
		));
	}
}
