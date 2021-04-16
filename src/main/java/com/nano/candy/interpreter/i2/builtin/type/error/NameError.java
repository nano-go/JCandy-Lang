package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;

@BuiltinClass(value = "NameError", isInheritable = true)
public class NameError extends ErrorObj {
	public static final CandyClass NAME_ERROR_CLASS = 
		BuiltinClassFactory.generate(NameError.class, ERROR_CLASS);
	
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
