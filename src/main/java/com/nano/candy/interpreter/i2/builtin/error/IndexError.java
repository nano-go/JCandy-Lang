package com.nano.candy.interpreter.i2.builtin.error;

import com.nano.candy.interpreter.i2.error.CandyRuntimeError;

public class IndexError extends CandyRuntimeError {
	
	public static void checkIndex(long index, int size) {
		if (index >= size || index < 0) {
			throw new IndexError(
				"list index out of range: index %d, size %d.", index, size);
		}
	}
	
	public IndexError(String format, Object... args) {
		super(format, args);
	}
}
