package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;

@BuiltinClass(value = "AssertionError", isInheritable = true)
public class AssertionError extends ErrorObj {
	public static final CandyClass ASSERTION_ERROR_CLASS = 
		BuiltinClassFactory.generate(AssertionError.class, ERROR_CLASS);
	
	public AssertionError() {
		super(ASSERTION_ERROR_CLASS);
	}
	
	public AssertionError(String msg) {
		super(ASSERTION_ERROR_CLASS, msg);
	}
}
