package com.nano.candy.interpreter.builtin.type.error;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.type.error.CompilerError;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;

@NativeClass(name = "CompilerError", isInheritable = true)
public class CompilerError extends ErrorObj {
	public static final CandyClass COMPILER_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(CompilerError.class, ERROR_CLASS);
	
	public CompilerError() {
		super(COMPILER_ERROR_CLASS);
	}
	
	public CompilerError(String errmsg) {
		super(COMPILER_ERROR_CLASS, errmsg);
	}
}
