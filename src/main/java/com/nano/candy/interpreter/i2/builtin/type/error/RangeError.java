package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.RangeError;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;

@NativeClass(name = "RangeError", isInheritable = true)
public class RangeError extends ErrorObj {
	public static final CandyClass RANGE_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(RangeError.class, ERROR_CLASS);
	
	public static int checkIndexForAdd(long index, int size) {
		return checkIndex(index, size + 1);
	}
		
	public static int checkIndex(long index, int size) {
		if (index >= size || index < -size) {
			new RangeError(
				"index out of range: index %d, size %d.",
				index, size
			).throwSelfNative();
		}
		return (int) (index < 0 ? size+index : index);
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
