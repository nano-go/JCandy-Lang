package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;

@NativeClass(name = "ArgumentError", isInheritable = true)
public class ArgumentError extends ErrorObj {

	public static final CandyClass ARGUMENT_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(ArgumentError.class, ERROR_CLASS);
	
	public static void checkArity(CallableObj callable, int expectedArity) {
		if (callable.vaargIndex() >= 0) {
			if (expectedArity < callable.arity()-1) {
				new ArgumentError
					(callable, expectedArity).throwSelfNative();
			}
			return;
		}
		if (callable.arity() != expectedArity) {
			new ArgumentError
				(callable, expectedArity).throwSelfNative();
		}
	}
	
	public static void throwsArgumentError(CallableObj callable, 
	                                       int actual) {
		if (callable.vaargIndex() != -1) {
			new ArgumentError(
				"The %s takes %d+ arguments, but %d were given.",
				name(callable), callable.arity()-1, actual
			).throwSelfNative();
		} else {
			new ArgumentError(callable, actual).throwSelfNative();
		}
	}
	
	public static void checkValueTooLarge(long value, String argName) {
		if (value > Integer.MAX_VALUE) {
			new ArgumentError("The %s too large.", argName)
				.throwSelfNative();
		}
	}

	private static String name(CandyObject obj) {
		if (obj instanceof CallableObj) {
			return ((CallableObj) obj).fullName();
		}
		return obj.getCandyClass().getName();
	}
	
	public ArgumentError() {
		super(ARGUMENT_ERROR_CLASS);
	}

	public ArgumentError(CallableObj callable, int actual) {
		this("The %s takes %d arguments, but %d were given.",
			 name(callable), callable.arity(), actual
		);
	}
	
	public ArgumentError(CallableObj callable, int arity, int actual) {
		this("The %s takes %d arguments, but %d were given.",
			 name(callable), arity, actual
		);
	}
	
	public ArgumentError(String msg) {
		super(ARGUMENT_ERROR_CLASS, msg);
	}    

	public ArgumentError(String msgFmt, Object... args) {
		super(ARGUMENT_ERROR_CLASS, msgFmt, args);
	}    
}
