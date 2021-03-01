package com.nano.candy.interpreter.i2.error;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;

public class TypeError extends CandyRuntimeError {
	
	public static void checkIsCallable(CandyObject callable) {
		if (!callable.isCallable()) {
			throw new TypeError("The '%s' obj is not callable.", 
				callable.getCandyClassName());
		}
	}
	
	public static void checkTypeMatched(CandyClass expected, CandyObject actual) {
		if (!expected.isSuperClassOf(actual.getCandyClass())) {
			throw new TypeError("The '%s' can't apply to '%s' obj.",
				actual.getCandyClass().getClassName(), expected.getClassName());	
		}
	}
	
	public static void checkTypeMatched(CandyClass expected, CandyObject actual, String msg, Object... args) {
		if (!expected.isSuperClassOf(actual.getCandyClass())) {
			throw new TypeError(msg, args);
		}
	}
	
	
	public TypeError(String format, Object... args) {
		super(String.format(format, args));
	}
}
