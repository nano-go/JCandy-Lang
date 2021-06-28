package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.AssertionError;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;

@NativeClass(name = "AssertionError", isInheritable = true)
public class AssertionError extends ErrorObj {
	public static final CandyClass ASSERTION_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(AssertionError.class, ERROR_CLASS);
	
	public AssertionError() {
		super(ASSERTION_ERROR_CLASS);
	}
	
	public AssertionError(String msg) {
		super(ASSERTION_ERROR_CLASS, msg);
	}
}
