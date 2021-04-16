package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.RangeError;

@BuiltinClass(value = "RangeError", isInheritable = true)
public class RangeError extends ErrorObj {
	public static final CandyClass RANGE_ERROR_CLASS = 
		BuiltinClassFactory.generate(RangeError.class, ERROR_CLASS);
	
	public static void checkIndexForAdd(long index, int size) {
		checkIndex(index, size + 1);
	}
		
	public static void checkIndex(long index, int size) {
		if (index < 0 || index >= size) {
			new RangeError(
				"index out of range: index %d, size %d.",
				index, size
			).throwSelfNative();
		}
	}
		
	public RangeError() {
		super(RANGE_ERROR_CLASS);
	}
	
	public RangeError(String message) {
		super(RANGE_ERROR_CLASS, message);
	}
	
	public RangeError(String msgFmt, Object... args) {
		super(RANGE_ERROR_CLASS, msgFmt, args);
	}
}
