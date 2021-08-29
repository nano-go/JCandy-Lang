package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.Range;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeMethod;
import com.nano.candy.std.Names;
import java.util.Objects;

@NativeClass(name = "Range", isInheritable = true)
public class Range extends CandyObject {
	
	public static final CandyClass RANGE_CLASS = 
		NativeClassRegister.generateNativeClass(Range.class);
	
	public Range() {
		super(RANGE_CLASS);
	}

	public Range(long left, long right) {
		super(RANGE_CLASS);
		setMetaData("left", IntegerObj.valueOf(left));
		setMetaData("right", IntegerObj.valueOf(right));
	}

	public long getLeft() {
		return ObjectHelper.asInteger(getMetaData("left"));
	}
	
	public CandyObject getLeftObj() {
		return getMetaData("left");
	}

	public long getRight() {
		return ObjectHelper.asInteger(getMetaData("right"));
	}
	
	public CandyObject getRightObj() {
		return getMetaData("right");
	}
	
	public long length() {
		return Math.abs(getRight() - getLeft());
	}

	@Override
	public CandyObject iterator(CNIEnv env) {
		return new IteratorObj.RangeIterator(getLeft(), getRight());
	}
	
	@Override
	public String toString() {
		return String.format("[%d, %d)", getLeft(), getRight());
	}

	@Override
	public IntegerObj hashCode(CNIEnv env) {
		return IntegerObj.valueOf(Objects.hash(getLeft(), getRight()));
	}

	@Override
	public BoolObj equals(CNIEnv env, CandyObject operand) {
		if (operand == this) {
			return BoolObj.TRUE;
		}
		if (operand instanceof Range) {
			Range r = (Range) operand;
			return BoolObj.valueOf(
				r.getLeft() == getLeft() &&
				r.getRight() == getRight()
			);
		}
		return BoolObj.FALSE;
	}

	/*===================== Native Methods ===================*/

	@NativeMethod(name = Names.METHOD_INITALIZER, argc = 2)
	public CandyObject initializer(CNIEnv env, CandyObject[] args) {
		setMetaData("left", args[0]);
		setMetaData("right", args[1]);
		return this;
	}

	@NativeMethod(name = "rand")
	public CandyObject rand(CNIEnv env, CandyObject[] args) {
		long left = getLeft();
		long right = getRight();
		if (left == right) {
			new TypeError("Empty set: %s", toString()).throwSelfNative();
		}
		long rand = left + (long) (Math.random() * (right - left)) ;
		return IntegerObj.valueOf(rand);
	}
}
