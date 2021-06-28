package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.NameError;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;

@NativeClass(name = "NameError", isInheritable = true)
public class NameError extends ErrorObj {
	public static final CandyClass NAME_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(NameError.class, ERROR_CLASS);
	
	public NameError() {
		super(NAME_ERROR_CLASS);
	}
		
	public NameError(String msg) {
		super(NAME_ERROR_CLASS, msg);
	}
	
	public NameError(String msgFmt, Object... args) {
		super(NAME_ERROR_CLASS, msgFmt, args);
	}
}
