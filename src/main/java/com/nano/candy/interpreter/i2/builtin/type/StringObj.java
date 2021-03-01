package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.error.IndexError;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.error.TypeError;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.StringFunctions;

@BuiltinClass("String")
public class StringObj extends BuiltinObject {

	public static final CandyClass STRING_CLASS = BuiltinClassFactory.generate(StringObj.class);
	public static final StringObj EMPTY_STR = new StringObj("");
	public static final StringObj EMPTY_LIST = new StringObj("[]");
	
	public static StringObj valueOf(String val) {
		return new StringObj(val);
	}
	
	private String value;
	private IntegerObj hashCode;
	
	public StringObj(String value) {
		super(STRING_CLASS);
		this.value = value;
	}
	
	public String value() {
		return value;
	}

	@Override
	public CandyObject getItem(CandyObject key) {
		return super.getItem(key);
	}

	@Override
	public CandyObject setItem(CandyObject key, CandyObject value) {
		return super.setItem(key, value);
	}
	
	@Override
	public CandyObject add(VM vm, CandyObject operand) {
		StringObj str = operand.strApiExeUser(vm);
		return valueOf(value + str.value);
	}

	@Override
	public CandyObject mul(VM vm, CandyObject operand) {
		if (operand instanceof IntegerObj) {
			long c = ((IntegerObj) operand).value;
			return valueOf(StringFunctions.repeat(value, c));
		}
		return super.mul(vm, operand);
	}

	@Override
	public BoolObj equals(VM vm, CandyObject operand) {
		if (operand == this) {
			return BoolObj.TRUE;
		}
		if (operand instanceof StringObj) {
			String str = ((StringObj)operand).value();
			return BoolObj.valueOf(
				StringFunctions.equals(value, str)
			);
		}
		return BoolObj.FALSE;
	}

	@Override
	public StringObj str(VM vm) {
		return this;
	}

	@Override
	public String toString() {
		return StringFunctions.valueOf(value);
	}

	@Override
	public IntegerObj hashCode(VM vm) {
		if (hashCode == null)
			hashCode = IntegerObj.valueOf(value.hashCode());
		return hashCode;
	}
	
	@BuiltinMethod(name = "substr", argc = 2)
	public void substring(VM vm) {
		long beginIndex = ObjectHelper.asInteger(vm.pop());
		long endIndex = ObjectHelper.asInteger(vm.pop());
		final int size = value.length();
		IndexError.checkIndex(beginIndex, size);
		IndexError.checkIndex(endIndex, size);
		if (beginIndex > endIndex) {
			vm.returnFromVM(EMPTY_STR);
			return;
		}
		vm.returnFromVM(valueOf(value.substring(
			(int) beginIndex, (int) endIndex)));
	}
	
	@BuiltinMethod(name = "length")
	public void length(VM vm) {
		vm.returnFromVM(IntegerObj.valueOf(value.length()));
	}
	
	@BuiltinMethod(name = "startWith", argc = 1)
	public void startWith(VM vm) {
		String str = vm.pop().strApiExeUser(vm).value;
		vm.returnFromVM(BoolObj.valueOf(value.startsWith(str)));
	}
	
	@BuiltinMethod(name = "endWith", argc = 1)
	public void endWith(VM vm) {
		String str = vm.pop().strApiExeUser(vm).value;
		vm.returnFromVM(BoolObj.valueOf(value.endsWith(str)));
	}
	
	@BuiltinMethod(name = "join", argc = 1)
	public void join(VM vm) {
		CandyObject obj = vm.pop();
		TypeError.checkTypeMatched(ArrayObj.ARRAY_CLASS, obj);
		ArrayObj array = (ArrayObj) obj;
		final int size = array.size();
		if (size == 0) {
			vm.returnFromVM(EMPTY_STR);
			return;
		}
		
		StringBuilder builder = new StringBuilder();
		int i = 0;
		int iMax = size-1;
		for (;;) {
			builder.append(array.get(i).strApiExeUser(vm).value);
			if (i == iMax) {
				break;
			}
			i ++;
			builder.append(this.value);
		}
		vm.returnFromVM(valueOf(builder.toString()));
	}
	
	@BuiltinMethod(name = "toUpperCase") 
	public void toUpperCase(VM vm){
		vm.returnFromVM(valueOf(value.toUpperCase()));
	}
	
	@BuiltinMethod(name = "toLowerCase") 
	public void toLowerCase(VM vm){
		vm.returnFromVM(valueOf(value.toLowerCase()));
	}
}
