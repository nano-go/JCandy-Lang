package com.nano.candy.interpreter.i2.error;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;

public class ArgumentError extends CandyRuntimeError {
	
	public static void checkArity(CandyObject callable, int actual) {
		if (callable.arity() != actual) {
			throw new ArgumentError(callable, actual);
		}
	}
	
	private static String name(CandyObject obj) {
		if (obj instanceof CallableObj) {
			return ((CallableObj) obj).name();
		}
		return obj.getCandyClass().getClassName();
	}
	
	public ArgumentError(CandyObject callable, int actual) {
		this("The %s takes %d arguments, but %d were given.",
			 name(callable), callable.arity(), actual
		);
	}    
	
	public ArgumentError(String format, Object... args) {
		super(format, args);
	}
}
