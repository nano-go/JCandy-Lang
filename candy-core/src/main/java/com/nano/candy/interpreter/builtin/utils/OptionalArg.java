package com.nano.candy.interpreter.builtin.utils;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.BoolObj;
import com.nano.candy.interpreter.builtin.type.IntegerObj;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.interpreter.builtin.type.StringObj;
import java.util.function.Supplier;

/**
 * The implementation of the optional argument in CNI.
 *
 * <p>An `OptionalArg` represents a parameter that can be ignored in CNI.
 *
 * <p>Usage:
 * <pre>
 * // Write a Java method for Candy language, which the parameter 'b'
 * // can be ignored.
 * @NativeMethod(name = "foo")
 * public CandyObject foo(CandyObject a, OptionalArg b) {
 *     ...
 *     CandyObject bVal = b.getValue("b");
 *     return new ArrayObj(new CandyObject[] {a, bVal});
 * }
 *
 * // In Candy language
 * foo(1) // [1, "b"]
 * foo(1, 2) // [1, 2]
 * </pre>
 */
public class OptionalArg {
	
	private final CandyObject arg;

	public OptionalArg(CandyObject arg) {
		this.arg = arg;
	}
	
	public CandyObject getValue(Supplier<CandyObject> defaultValue) {
		return this.arg != NullPointer.undefined() ? arg :
			defaultValue.get();
	}
	
	public CandyObject getValue(CandyObject defaultValue) {
		return this.arg != NullPointer.undefined() ? arg :
			defaultValue;
	}
	
	public CandyObject getValue(String defaultValue) {
		return this.arg != NullPointer.undefined() ? arg :
			StringObj.valueOf(defaultValue);
	}
	
	public CandyObject getValue(long defaultValue) {
		return this.arg != NullPointer.undefined() ? arg :
			IntegerObj.valueOf(defaultValue);
	}
	
	public CandyObject getValue(boolean defaultValue) {
		return this.arg != NullPointer.undefined() ? arg :
			BoolObj.valueOf(defaultValue);
	}
	
	public boolean isPresent() {
		return this.arg != NullPointer.undefined();
	}
	
	public CandyObject getValue() {
		return this.arg;
	}
}
