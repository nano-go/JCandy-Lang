package com.nano.candy.interpreter.error;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.Callable;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;

public class TypeError extends CandyRuntimeError {
	
	public static Callable checkCallable(CandyObject obj) {
		return checkCallable(obj, obj.getClassName());
	}
	
	public static Callable checkCallable(CandyObject obj, String name) {
		if (obj instanceof Callable) {
			return (Callable) obj;
		}
		throw new TypeError("'%s' is not callable.", name);
	}
	
	public static void checkTypeMatched(CandyClass expected, CandyObject actual) {
		if (expected.isSuperclassOf(actual._class())) {
			return;
		}
		throw new TypeError(expected, actual);
	}
	
	public static void checkTypeMatched(CandyClass expected, CandyObject actual, String messageFormat) {
		if (expected.isSuperclassOf(actual._class())) {
			return;
		}
		throw new TypeError(expected, actual, messageFormat);
	}
	
	public TypeError(String msg, Object... args) {
		super(String.format(msg, args)) ;
	}
	
	public TypeError(CandyClass expected, CandyObject actual) {
		this(expected, actual, "Expected '%s', but '%s'.");
	}
	
	public TypeError(CandyClass expected, CandyObject actual, String messageFormat) {
		this(
			messageFormat,
			expected.stringValue().value(),
			actual._class().stringValue().value()
		);
	}
}
