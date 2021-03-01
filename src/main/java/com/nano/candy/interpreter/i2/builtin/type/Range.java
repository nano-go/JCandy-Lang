package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.error.CandyRuntimeError;
import com.nano.candy.interpreter.i2.vm.VM;
import java.util.Objects;

@BuiltinClass("Range")
public class Range extends BuiltinObject {
	
	public static final CandyClass RANGE_CLASS = BuiltinClassFactory.generate(Range.class);
	
	protected static class RangeIterator extends IteratorObj {
		private int v;
		private long left, right;

		public RangeIterator(long left, long right) {
			this.left = left;
			this.right = right;
			this.v = left > right ? -1 : 1;
		}
		
		@Override
		public boolean hasNext(VM vm) {
			return left != right;
		}

		@Override
		public CandyObject next(VM vm) {
			IntegerObj next = IntegerObj.valueOf(left);
			left += v;
			return next;
		}
	}
	
	long left, right;
	
	public Range() {
		super(RANGE_CLASS);
	}

	public Range(long left, long right) {
		super(RANGE_CLASS);
		this.left = left;
		this.right = right;
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
	public CandyObject iterator() {
		return new RangeIterator(left, right);
	}
	
	@Override
	public CandyObject setAttr(VM vm, String attr, CandyObject ref) {
		switch (attr) {
			case "left":
				left = ObjectHelper.asInteger(ref);
				return ref;
			case "right":
				right = ObjectHelper.asInteger(ref);
				return ref;
		}
		return super.setAttr(vm, attr, ref);
	}

	@Override
	public CandyObject getAttr(VM vm, String attr) {
		switch (attr) {
			case "left":
				return IntegerObj.valueOf(left);
			case "right":
				return IntegerObj.valueOf(right);
		}
		return super.getAttr(vm, attr);
	}
	
	@Override
	public String toString() {
		return String.format("[%d, %d)", left, right);
	}

	@Override
	public IntegerObj hashCode(VM vm) {
		return IntegerObj.valueOf(Objects.hash(left, right));
	}

	/*===================== Built-in Methods ===================*/

	@BuiltinMethod(name = "", argc = 2)
	public void initializer(VM vm) {
		this.left = ObjectHelper.asInteger(vm.pop());
		this.right = ObjectHelper.asInteger(vm.pop());
		vm.returnFromVM(this);
	}

	@BuiltinMethod(name = "rand")
	public void rand(VM vm) {
		if (left == right) {
			throw new CandyRuntimeError("Empty set: %s", toString());
		}
		long rand = left + (long) (Math.random() * (right - left)) ;
		vm.returnFromVM(IntegerObj.valueOf(rand));
	}

	@BuiltinMethod(name = "toArray")
	public void toArray(VM vm) {
		long length = length();
		ArrayObj.checkCapacity(length);
		int size = (int) length;
		CandyObject[] elements = new CandyObject[size];
		int v = left > right ? -1 : 1;
		long e = left;
		for (int i = 0; i < size; i ++, e += v) {
			elements[i] = IntegerObj.valueOf(e);
		}
		vm.returnFromVM(new ArrayObj(elements, size));
	}
}
