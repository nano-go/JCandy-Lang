package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;

@NativeClass(name = "ArgumentError", isInheritable = true)
public class ArgumentError extends ErrorObj {

	public static final CandyClass ARGUMENT_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(ArgumentError.class, ERROR_CLASS);
	
	public static void checkArity(CandyObject callable, int expectedArity) {
		if (callable.arity() != expectedArity) {
			new ArgumentError(callable, expectedArity)
				.throwSelfNative();
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
