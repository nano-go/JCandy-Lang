package com.nano.candy.interpreter.builtin.type.error;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.error.ErrorObj;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;

@NativeClass(name = "TypeError", isInheritable = true)
public class TypeError extends ErrorObj {
	public static final CandyClass TYPE_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(TypeError.class, ERROR_CLASS);

	public static CallableObj requiresCallable(CandyObject callable) {
		checkIsCallable(callable);
		return (CallableObj) callable;
	}
	
	public static CandyClass requiresClass(CandyObject obj) {
		if (obj instanceof CandyClass) {
			return (CandyClass) obj;
		}
		new TypeError("The '%s' object is not a Class.", 
			obj.toString()).throwSelfNative();
		return null;
	}
		
	public static void checkIsCallable(CandyObject callable) {
		if (!callable.isCallable()) {
			new TypeError("The '%s' object is not callable.", 
				callable.getCandyClassName()
			).throwSelfNative();
		}
	}
	
	public static void checkClassMatched(CandyClass expected, CandyClass clazz) {
		if (clazz != expected) {
			new TypeError("The '%s' class can't apply to '%s' class.",
				clazz.getCandyClassName(), expected.getName()
			).throwSelfNative();
		}
	}

	public static void checkTypeMatched(CandyClass expected, CandyObject instance) {
		if (!expected.isSuperClassOf(instance.getCandyClass())) {
			new TypeError(
				"The '%s' can't apply to '%s' obj.",
				instance.getCandyClassName(), expected.getName()
			).throwSelfNative();	
		}
	}
	
	public static void checkIsInstanceOf(CandyClass expected, CandyObject instance) {
		if (!instance.isInstanceOf(expected)) {
			new TypeError(
				"The '%s' can't apply to '%s' obj.",
				instance.getCandyClassName(), expected.getName()
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
	
	public TypeError(String msg) {
		super(TYPE_ERROR_CLASS, msg);
	}
}
