package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.error.TypeError;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.utils.ArrayUtils;
import java.util.Objects;

@BuiltinClass("Tuple")
public class TupleObj extends BuiltinObject {
	public static final CandyClass TUPLE_CLASS = BuiltinClassFactory.generate(TupleObj.class);
	public static final TupleObj EMPTY_TUPLE = new TupleObj(ArrayObj.EMPTY_ARRAY);
	
	private CandyObject[] elements;

	public TupleObj() {
		super(TUPLE_CLASS);
		this.elements = ArrayObj.EMPTY_ARRAY;
	}
	
	public TupleObj(CandyObject[] elements) {
		super(TUPLE_CLASS);
		this.elements = Objects.requireNonNull(elements);
	}

	@Override
	public CandyObject iterator() {
		return new IteratorObj.ArrIterator(elements, elements.length);
	}

	@Override
	public CandyObject getItem(CandyObject key) {
		return elements[(int) ObjectHelper.asInteger(key)];
	}
	
	@Override
	public CandyObject add(VM vm, CandyObject operand) {
		TypeError.checkTypeMatched(TUPLE_CLASS, operand);
		return new TupleObj(ArrayUtils.mergeArray(
			this.elements, ((TupleObj) operand).elements
		));
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
					this.elements[i].equalsApiExeUser(vm, es[i]);
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
			return StringObj.valueOf("()");
		}
		StringBuilder builder = new StringBuilder("(");
		builder.append(elements[0].strApiExeUser(vm).value());
		for (int i = 1; i < elements.length; i ++) {
			builder.append(", ");
			builder.append(elements[i].strApiExeUser(vm).value());
		}
		builder.append(")");
		return StringObj.valueOf(builder.toString());
	}
	
	@BuiltinMethod(name = "len")
	public void len(VM vm) {
		vm.returnFromVM(IntegerObj.valueOf(elements.length));
	}
}
