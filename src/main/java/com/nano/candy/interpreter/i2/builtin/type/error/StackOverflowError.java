package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.StackOverflowError;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;

@NativeClass(name = "StackOverflowError", isInheritable = true)
public class StackOverflowError extends ErrorObj {
	public static final CandyClass SOF_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(StackOverflowError.class, ERROR_CLASS);

	protected StackOverflowError() {
		super(SOF_ERROR_CLASS);
	}
		
	public StackOverflowError(int maxDeepth) {
		super(SOF_ERROR_CLASS, "maximum stack deepth(" + maxDeepth
			+ ") exceeded");
	}

	public StackOverflowError(String message) {
		super(SOF_ERROR_CLASS, message);
	}
}
