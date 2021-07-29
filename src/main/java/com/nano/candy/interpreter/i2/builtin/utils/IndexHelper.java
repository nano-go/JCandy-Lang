package com.nano.candy.interpreter.i2.builtin.utils;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.error.RangeError;

public class IndexHelper {
	public static int asIndex(CandyObject index, int size) {
		long i = ObjectHelper.asInteger(index);
		return RangeError.checkIndex(i, size);
	}
	
	public static int asIndexForAdd(CandyObject index, int size) {
		long i = ObjectHelper.asInteger(index);
		return RangeError.checkIndexForAdd(i, size);
	}
}
