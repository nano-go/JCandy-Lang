package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.TupleObj;
import com.nano.candy.interpreter.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.builtin.type.error.RangeError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.builtin.utils.ArrayHelper;
import com.nano.candy.interpreter.builtin.utils.IndexHelper;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeMethod;
import com.nano.candy.std.Names;
import com.nano.candy.utils.ArrayUtils;
import java.util.Arrays;
import java.util.Objects;

@NativeClass(name = "Tuple", isInheritable=true)
public final class TupleObj extends CandyObject {
	public static final CandyClass TUPLE_CLASS = 
		NativeClassRegister.generateNativeClass(TupleObj.class);
	
	public static final TupleObj EMPTY_TUPLE = new TupleObj(ArrayObj.EMPTY_ARRAY);
	
	private CandyObject[] elements;
	private IntegerObj hash;

	public TupleObj() {
		super(TUPLE_CLASS);
		this.elements = ArrayObj.EMPTY_ARRAY;
		this.hash = IntegerObj.valueOf(0);
	}
	
	public TupleObj(CandyObject[] elements) {
		super(TUPLE_CLASS);
		this.elements = Objects.requireNonNull(elements);
		this.hash = null;
	}
	
	public TupleObj(CandyObject[] elements, int len) {
		super(TUPLE_CLASS);
		this.elements = Arrays.copyOf(elements, len);
		this.hash = null;
	}
	
	public ArrayObj toArrayObj() {
		return new ArrayObj(this.elements);
	}
	
	public int length() {
		return elements.length;
	}
	
	public CandyObject get(long index) {
		index = RangeError.checkIndex(index, elements.length);
		return elements[(int) index];
	}
	
	public TupleObj add(CandyObject tuple) {
		TypeError.checkTypeMatched(TUPLE_CLASS, tuple);
		return new TupleObj(ArrayUtils.mergeArray(
			this.elements, ((TupleObj) tuple).elements
		));
	}
	
	public CandyObject[] subarray(Range range) {
		return subarray(range.getLeftObj(), range.getRightObj());
	}

	public CandyObject[] subarray(CandyObject begin, CandyObject end) {
		int beginIndex = IndexHelper.asIndex(begin, elements.length);
		int endIndex = IndexHelper.asIndexForAdd(end, elements.length);
		return privateSubarray(beginIndex, endIndex);
	}
		
	/**
	 * We assume that the begin index and the end index are invalid.
	 */
	private CandyObject[] privateSubarray(int begin, int end) {
		if (end - begin <= 0) {
			return ArrayObj.EMPTY_ARRAY;
		}
		return Arrays.copyOfRange(elements, begin, end);
	}

	@Override
	public CandyObject iterator(CNIEnv env) {
		return new IteratorObj.ArrIterator(elements, elements.length);
	}

	@Override
	public CandyObject getItem(CNIEnv env, CandyObject key) {
		if (key instanceof Range) {
			return new TupleObj(subarray((Range) key));
		}
		long index = ObjectHelper.asInteger(key);
		return ObjectHelper.preventNull(get(index));
	}
	
	@Override
	public CandyObject add(CNIEnv env, CandyObject operand) {
		return add(operand);
	}

	@Override
	protected CandyObject mul(CNIEnv env, CandyObject operand) {
		long repeat = ObjectHelper.asInteger(operand);
		ArgumentError.checkValueTooLarge(repeat, "repeat");
		return new TupleObj(ArrayUtils.repeat(elements, (int) repeat));
	}

	@Override
	public IntegerObj hashCode(CNIEnv env) {
		if (hash != null) return hash;
		return hash = IntegerObj.valueOf(
			ArrayHelper.hashCode(env, elements, 0, elements.length)
		);
	}

	@Override
	public BoolObj equals(CNIEnv env, CandyObject operand) {
		if (operand == this) {
			return BoolObj.TRUE;
		}
		if (operand instanceof TupleObj) {
			CandyObject[] es = ((TupleObj) operand).elements;
			if (this.elements.length != es.length) {
				return BoolObj.FALSE;
			}
			for (int i = 0; i < es.length; i ++) {
				BoolObj res = 
					this.elements[i].callEquals(env, es[i]);
				if (!res.value()) {
					return BoolObj.FALSE;
				}
			}
			return BoolObj.TRUE;
		}
		return super.equals(env, operand);
	}

	@Override
	public StringObj str(CNIEnv env) {
		if (elements.length == 0) {
			return StringObj.EMPTY_TUPLE;
		}
		StringBuilder builder = new StringBuilder("(");
		builder.append(ArrayHelper.toString(env, elements, ", "));
		builder.append(")");
		return StringObj.valueOf(builder.toString());
	}
	
	@NativeMethod(name = Names.METHOD_INITALIZER, argc = 1)
	public CandyObject init(CNIEnv env, CandyObject[] args) {
		if (args[0] instanceof  TupleObj) {
			TupleObj tuple = (TupleObj) args[0];
			this.elements = Arrays.copyOf(
				tuple.elements, tuple.elements.length
			);
		} else if (args[0] instanceof ArrayObj) {
			ArrayObj arr = (ArrayObj) args[0];
			this.elements = Arrays.copyOfRange(
				arr.elements, 0, arr.length()
			);
		} else {
			this.elements = ObjectHelper.iterableObjToArray(env, args[0]);
		}
		return this;
	}
	
	@NativeMethod(name = "length")
	public CandyObject len(CNIEnv env, CandyObject[] args) {
		return IntegerObj.valueOf(elements.length);
	}
}
