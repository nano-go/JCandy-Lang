package com.nano.candy.interpreter.builtin.type.error;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;

@NativeClass(name = "OverrideError", isInheritable = true)
public class OverrideError extends ErrorObj {
	public static final CandyClass OVERRIDE_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(OverrideError.class, ERROR_CLASS);

	public OverrideError() {
		super(OVERRIDE_ERROR_CLASS);
	}

	public OverrideError(String errmsg) {
		super(OVERRIDE_ERROR_CLASS, errmsg);
	}
	
	public OverrideError(String errmsg, Object... args) {
		super(OVERRIDE_ERROR_CLASS, errmsg, args);
	}
}
