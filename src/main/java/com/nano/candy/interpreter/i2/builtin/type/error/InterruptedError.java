package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.AssertionError;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;

@NativeClass(name = "InterruptedError", isInheritable = true)
public class InterruptedError extends ErrorObj {
	public static final CandyClass INRERRUPTED_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(InterruptedError.class, ERROR_CLASS);

	public InterruptedError() {
		super(INRERRUPTED_ERROR_CLASS);
	}
	
	public InterruptedError(InterruptedException e) {
		this(e.getMessage());
	}

	public InterruptedError(String msg) {
		super(INRERRUPTED_ERROR_CLASS, msg);
	}
}
