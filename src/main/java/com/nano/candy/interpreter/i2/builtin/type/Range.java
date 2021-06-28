package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.Range;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import java.util.Objects;

@NativeClass(name = "Range")
public class Range extends CandyObject {
	
	public static final CandyClass RANGE_CLASS = 
		NativeClassRegister.generateNativeClass(Range.class);
	
	long left, right;
	
	public Range() {
		super(RANGE_CLASS);
	}

	public Range(long left, long right) {
		super(RANGE_CLASS);
		this.left = left;
		this.right = right;
		setMetaData("left", IntegerObj.valueOf(right));
		setMetaData("right", IntegerObj.valueOf(right));
		freeze();
	}

	public long getLeft() {
		return left;
	}

	public long getRight() {
		return right;
	}
	
	public long length() {
		return Math.abs(right - left);
	}

	@Override
	public CandyObject iterator(VM vm) {
		return new IteratorObj.RangeIterator(left, right);
	}
	
	@Override
	public String toString() {
		return String.format("[%d, %d)", left, right);
	}

	@Override
	public IntegerObj hashCode(VM vm) {
		return IntegerObj.valueOf(Objects.hash(left, right));
	}

	/*===================== Native Methods ===================*/

	@NativeMethod(name = Names.METHOD_INITALIZER, argc = 2)
	public CandyObject initializer(VM vm, CandyObject[] args) {
		this.left = ObjectHelper.asInteger(args[0]);
		this.right = ObjectHelper.asInteger(args[1]);
		setMetaData("left", args[0]);
		setMetaData("right", args[1]);
		freeze();
		return this;
	}

	@NativeMethod(name = "rand")
	public CandyObject rand(VM vm, CandyObject[] args) {
		if (left == right) {
			new TypeError("Empty set: %s", toString()).throwSelfNative();
		}
		long rand = left + (long) (Math.random() * (right - left)) ;
		return IntegerObj.valueOf(rand);
	}

	@NativeMethod(name = "toArray")
	public CandyObject toArray(VM vm, CandyObject[] args) {
		long length = length();
		ArrayObj.checkCapacity(length);
		int size = (int) length;
		CandyObject[] elements = new CandyObject[size];
		int v = left > right ? -1 : 1;
		long e = left;
		for (int i = 0; i < size; i ++, e += v) {
			elements[i] = IntegerObj.valueOf(e);
		}
		return new ArrayObj(elements);
	}
}
