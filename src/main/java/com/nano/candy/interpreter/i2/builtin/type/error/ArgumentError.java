package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;

@BuiltinClass("ArgumentError")
public class ArgumentError extends ErrorObj {

	public static final CandyClass ARGUMENT_ERROR_CLASS = 
		BuiltinClassFactory.generate(ArgumentError.class, ERROR_CLASS);
	
	public static void checkArity(CandyObject callable, int actual) {
		if (callable.arity() != actual) {
			new ArgumentError(callable, actual).throwSelfNative();
		}
	}

	private static String name(CandyObject obj) {
		if (obj instanceof CallableObj) {
			return ((CallableObj) obj).name();
		}
		return obj.getCandyClass().getClassName();
	}
	
	public ArgumentError() {
		super(ARGUMENT_ERROR_CLASS);
	}

	public ArgumentError(CandyObject callable, int actual) {
		this("The %s takes %d arguments, but %d were given.",
			 name(callable), callable.arity(), actual
		);
	}
	
	public ArgumentError(String msg) {
		super(ARGUMENT_ERROR_CLASS, msg);
	}    

	public ArgumentError(String msgFmt, Object... args) {
		super(ARGUMENT_ERROR_CLASS, msgFmt, args);
	}    
}
