package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.RangeError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.StringFunctions;
import com.nano.candy.utils.ArrayUtils;
import java.util.ArrayList;

@BuiltinClass("String")
public class StringObj extends BuiltinObject {

	public static final CandyClass STRING_CLASS = BuiltinClassFactory.generate(StringObj.class);
	public static final StringObj EMPTY_STR = new StringObj("");
	public static final StringObj RECURSIVE_LIST = new StringObj("[...]");
	public static final StringObj EMPTY_LIST = new StringObj("[]");
	public static final StringObj EMPTY_TUPLE = new StringObj("()");
	
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
		RangeError.checkIndex(beginIndex, size);
		RangeError.checkIndexForAdd(endIndex, size);
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
	
	@BuiltinMethod(name = "split", argc = 1)
	public void split(VM vm) {
		String regex = ObjectHelper.asString(vm.pop());
		String[] res = value.split(regex);
		CandyObject[] elements = new CandyObject[res.length];
		for (int i = 0; i < res.length; i ++) {
			elements[i] = valueOf(res[i]);
		}
		vm.returnFromVM(new ArrayObj(elements));
	}
	
	@BuiltinMethod(name = "splitlines", argc = 0)
	public void splitlines(VM vm) {
		if (value.length() == 0) {
			vm.returnFromVM(new ArrayObj(0));
			return;
		}
		ArrayList<CandyObject> lines = new ArrayList<CandyObject>();
		char[] buffer = new char[Math.min(value.length(), 128)];
		int bp = 0;
		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i ++) {
			if (chars[i] == '\n') {
				lines.add(valueOf(String.valueOf(buffer, 0, bp)));
				bp = 0;
			} else {
				ArrayUtils.growCapacity(buffer, bp);
				buffer[bp ++] = chars[i];
			}
		}
		if (chars[chars.length-1] != '\n') {
			lines.add(valueOf(String.valueOf(buffer, 0, bp)));
		}
		vm.returnFromVM(new ArrayObj(lines.toArray(ArrayObj.EMPTY_ARRAY)));
	}
	
	@BuiltinMethod(name = "trim")
	public void trim(VM vm) {
		vm.returnFromVM(valueOf(value.trim()));
	}
	
	@BuiltinMethod(name = "replaceFirst", argc = 2)
	public void replace(VM vm) {
		String regex = ObjectHelper.asString(vm.pop());
		String replacement = vm.pop().strApiExeUser(vm).value;
		vm.returnFromVM(valueOf(value.replaceFirst(regex, replacement)));
	}
	

	@BuiltinMethod(name = "replaceAll", argc = 2)
	public void replaceAll(VM vm) {
		String regex = ObjectHelper.asString(vm.pop());
		String replacement = vm.pop().strApiExeUser(vm).value;
		vm.returnFromVM(valueOf(value.replaceAll(regex, replacement)));
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
