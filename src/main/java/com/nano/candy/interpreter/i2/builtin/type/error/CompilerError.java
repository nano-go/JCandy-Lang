package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;

@BuiltinClass(value = "CompilerError", isInheritable = true)
public class CompilerError extends ErrorObj {
	public static final CandyClass COMPILER_ERROR_CLASS = 
		BuiltinClassFactory.generate(CompilerError.class, ERROR_CLASS);
	
	public CompilerError() {
		super(COMPILER_ERROR_CLASS);
	}
	
	public CompilerError(String errmsg) {
		super(COMPILER_ERROR_CLASS, errmsg);
	}
}
