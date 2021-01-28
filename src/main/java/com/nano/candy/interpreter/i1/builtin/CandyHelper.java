package com.nano.candy.interpreter.i1.builtin;

import com.nano.candy.interpreter.error.ArgumentError;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.type.Callable;
import java.util.Comparator;

public class CandyHelper {

	public static final CandyObject[] EMPTY_ARGUMENT = {};
	
	public static Comparator<CandyObject> newComparator(/* AstInterpreter interpreter*/) {
		return new Comparator<CandyObject>(){
			@Override
			public int compare(CandyObject obj1, CandyObject obj2) {
				return obj1.compareTo(obj2);
			}
		};
	}

	public static boolean isCallable(CandyObject obj, int expectedArguments) {
		if (obj != null && obj instanceof Callable) {
			Callable callable = (Callable) obj;
			return callable.arity() == expectedArguments;
		}
		return false;
	}
	
	public static CandyObject invoke(AstInterpreter interpreter, Callable callable) {
		return invoke(interpreter, callable, EMPTY_ARGUMENT);
	}
	
	public static CandyObject invoke(AstInterpreter interpreter, Callable callable, CandyObject... args) {
		ArgumentError.checkArguments(callable.arity(), args.length);
		return callable.onCall(interpreter, args);
	}
}
