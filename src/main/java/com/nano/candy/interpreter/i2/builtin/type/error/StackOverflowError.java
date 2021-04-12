package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.StackOverflowError;

@BuiltinClass("StackOverflowError")
public class StackOverflowError extends ErrorObj {
	public static final CandyClass SOF_ERROR_CLASS = 
		BuiltinClassFactory.generate(StackOverflowError.class, ERROR_CLASS);

	public StackOverflowError() {
		super(SOF_ERROR_CLASS);
	}

	public StackOverflowError(String message) {
		super(SOF_ERROR_CLASS, message);
	}
}
