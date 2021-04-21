package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;

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
