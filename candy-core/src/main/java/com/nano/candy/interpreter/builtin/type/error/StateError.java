package com.nano.candy.interpreter.builtin.type.error;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.type.error.AssertionError;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;

@NativeClass(name = "StateError", isInheritable = true)
public class StateError extends ErrorObj {
	public static final CandyClass STATE_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(StateError.class, ERROR_CLASS);

	public StateError() {
		super(STATE_ERROR_CLASS);
	}

	public StateError(String msg) {
		super(STATE_ERROR_CLASS, msg);
	}
}
