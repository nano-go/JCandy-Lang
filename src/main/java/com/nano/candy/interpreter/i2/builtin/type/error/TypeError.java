package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.ErrorObj;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;

@NativeClass(name = "TypeError", isInheritable = true)
public class TypeError extends ErrorObj {
	public static final CandyClass TYPE_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(TypeError.class, ERROR_CLASS);
		
	public static void checkIsCallable(CandyObject callable) {
		if (!callable.isCallable()) {
			new TypeError("The '%s' obj is not callable.", 
				callable.getCandyClassName()
			).throwSelfNative();
		}
	}
	
	public static void checkClassMatched(CandyClass expected, CandyClass clazz) {
		if (clazz != expected) {
			new TypeError("The '%s' class can't apply to '%s' class.",
				clazz.getCandyClassName(), expected.getClassName()
			).throwSelfNative();
		}
	}

	public static void checkTypeMatched(CandyClass expected, CandyObject instance) {
		if (instance.isCandyClass() || !expected.isSuperClassOf(instance.getCandyClass())) {
			new TypeError(
				"The '%s' can't apply to '%s' obj.",
				instance.getCandyClassName(), expected.getClassName()
			).throwSelfNative();	
		}
	}

	public static void checkTypeMatched(CandyClass expected, CandyObject instance, String msg, Object... args) {
		if (instance.isCandyClass() || !expected.isSuperClassOf(instance.getCandyClass())) {
			new TypeError(msg, args).throwSelfNative();
		}
	}
	
	public TypeError() {
		super(TYPE_ERROR_CLASS);
	}
	
	public TypeError(String msgFmt, Object... args) {
		super(TYPE_ERROR_CLASS, msgFmt, args);
	}
}
