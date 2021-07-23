package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.TupleObj;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ArrayHelper;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.vm.VM;
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
	
	public int length() {
		return elements.length;
	}
	
	public CandyObject get(int index) {
		return elements[index];
	}
	
	public TupleObj add(CandyObject tuple) {
		TypeError.checkTypeMatched(TUPLE_CLASS, tuple);
		return new TupleObj(ArrayUtils.mergeArray(
			this.elements, ((TupleObj) tuple).elements
		));
	}

	@Override
	public CandyObject iterator(VM vm) {
		return new IteratorObj.ArrIterator(elements, elements.length);
	}

	@Override
	public CandyObject getItem(VM vm, CandyObject key) {
		return elements[(int) ObjectHelper.asInteger(key)];
	}
	
	@Override
	public CandyObject add(VM vm, CandyObject operand) {
		return add(operand);
	}

	@Override
	public IntegerObj hashCode(VM vm) {
		if (hash != null) return hash;
		return hash = IntegerObj.valueOf(
			ArrayHelper.hashCode(vm, elements, 0, elements.length)
		);
	}

	@Override
	public BoolObj equals(VM vm, CandyObject operand) {
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
					this.elements[i].callEquals(vm, es[i]);
				if (!res.value()) {
					return BoolObj.FALSE;
				}
			}
			return BoolObj.TRUE;
		}
		return super.equals(vm, operand);
	}

	@Override
	public StringObj str(VM vm) {
		if (elements.length == 0) {
			return StringObj.EMPTY_TUPLE;
		}
		StringBuilder builder = new StringBuilder("(");
		builder.append(ArrayHelper.toString(vm, elements, ", "));
		builder.append(")");
		return StringObj.valueOf(builder.toString());
	}
	
	@NativeMethod(name = Names.METHOD_INITALIZER, argc = 1)
	public CandyObject init(VM vm, CandyObject[] args) {
		if (args[0] instanceof  TupleObj) {
			TupleObj tuple = (TupleObj) args[0];
			this.elements = Arrays.copyOf(
				tuple.elements, tuple.elements.length
			);
		} else if (args[0] instanceof ArrayObj) {
			ArrayObj arr = (ArrayObj) args[0];
			this.elements = Arrays.copyOfRange(
				arr.getBuiltinArray(), 0, arr.length()
			);
		} else {
			this.elements = ObjectHelper.iterableObjToArray(vm, args[0]);
		}
		return this;
	}
	
	@NativeMethod(name = "length")
	public CandyObject len(VM vm, CandyObject[] args) {
		return IntegerObj.valueOf(elements.length);
	}
}
