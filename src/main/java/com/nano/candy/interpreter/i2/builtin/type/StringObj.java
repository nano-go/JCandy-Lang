package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.RangeError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.StringFunctions;
import com.nano.candy.utils.ArrayUtils;
import java.util.ArrayList;

@NativeClass(name = "String")
public class StringObj extends BuiltinObject {

	public static final CandyClass STRING_CLASS =
		NativeClassRegister.generateNativeClass(StringObj.class);
	
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
		return value;
	}

	@Override
	public IntegerObj hashCode(VM vm) {
		if (hashCode == null)
			hashCode = IntegerObj.valueOf(value.hashCode());
		return hashCode;
	}
	
	@NativeMethod(name = "substr", argc = 2)
	public CandyObject substring(VM vm, CandyObject[] args) {
		long beginIndex = ObjectHelper.asInteger(args[0]);
		long endIndex = ObjectHelper.asInteger(args[1]);
		final int size = value.length();
		RangeError.checkIndex(beginIndex, size);
		RangeError.checkIndexForAdd(endIndex, size);
		if (beginIndex > endIndex) {
			return EMPTY_STR;
		}
		return valueOf(value.substring(
			(int) beginIndex, (int) endIndex));
	}
	
	@NativeMethod(name = "length")
	public CandyObject length(VM vm, CandyObject[] args) {
		return IntegerObj.valueOf(value.length());
	}
	
	@NativeMethod(name = "startWith", argc = 1)
	public CandyObject startWith(VM vm, CandyObject[] args) {
		String str = args[0].strApiExeUser(vm).value;
		return BoolObj.valueOf(value.startsWith(str));
	}
	
	@NativeMethod(name = "endWith", argc = 1)
	public CandyObject endWith(VM vm, CandyObject[] args) {
		String str = args[0].strApiExeUser(vm).value;
		return BoolObj.valueOf(value.endsWith(str));
	}
	
	@NativeMethod(name = "split", argc = 1)
	public CandyObject split(VM vm, CandyObject[] args) {
		String regex = ObjectHelper.asString(args[0]);
		String[] res = value.split(regex);
		CandyObject[] elements = new CandyObject[res.length];
		for (int i = 0; i < res.length; i ++) {
			elements[i] = valueOf(res[i]);
		}
		return new ArrayObj(elements);
	}
	
	@NativeMethod(name = "splitlines")
	public CandyObject splitlines(VM vm, CandyObject[] args) {
		if (value.length() == 0) {
			return new ArrayObj(0);
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
				buffer = ArrayUtils.growCapacity(buffer, bp);
				buffer[bp ++] = chars[i];
			}
		}
		if (chars[chars.length-1] != '\n') {
			lines.add(valueOf(String.valueOf(buffer, 0, bp)));
		}
		return new ArrayObj(lines.toArray(ArrayObj.EMPTY_ARRAY));
	}
	
	@NativeMethod(name = "trim")
	public CandyObject trim(VM vm, CandyObject[] args) {
		return valueOf(value.trim());
	}
	
	@NativeMethod(name = "replaceFirst", argc = 2)
	public CandyObject replace(VM vm, CandyObject[] args) {
		String regex = ObjectHelper.asString(args[0]);
		String replacement = args[1].strApiExeUser(vm).value;
		return valueOf(value.replaceFirst(regex, replacement));
	}
	

	@NativeMethod(name = "replaceAll", argc = 2)
	public CandyObject replaceAll(VM vm, CandyObject[] args) {
		String regex = ObjectHelper.asString(args[0]);
		String replacement = args[1].strApiExeUser(vm).value;
		return valueOf(value.replaceAll(regex, replacement));
	}
	
	@NativeMethod(name = "join", argc = 1)
	public CandyObject join(VM vm, CandyObject[] args) {
		CandyObject obj = args[0];
		TypeError.checkTypeMatched(ArrayObj.ARRAY_CLASS, obj);
		ArrayObj array = (ArrayObj) obj;
		final int size = array.size();
		if (size == 0) {
			return EMPTY_STR;
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
		return valueOf(builder.toString());
	}
	
	@NativeMethod(name = "lastIndexOf", argc=1) 
	public CandyObject lastIndexOf(VM vm, CandyObject[] args){
		return IntegerObj.valueOf(
			value.lastIndexOf(ObjectHelper.asString(args[0]))
		);
	}
	
	@NativeMethod(name = "indexOf", argc=1) 
	public CandyObject indexOf(VM vm, CandyObject[] args){
		return IntegerObj.valueOf(
			value.indexOf(ObjectHelper.asString(args[0]))
		);
	}
	
	@NativeMethod(name = "toUpperCase") 
	public CandyObject toUpperCase(VM vm, CandyObject[] args){
		return valueOf(value.toUpperCase());
	}
	
	@NativeMethod(name = "toLowerCase") 
	public CandyObject toLowerCase(VM vm, CandyObject[] args){
		return valueOf(value.toLowerCase());
	}
	
	@NativeMethod(name = "toInt")
	public CandyObject toInt(VM vm, CandyObject[] args){
		return IntegerObj.valueOf(Integer.valueOf(value));
	}
	
	@NativeMethod(name = "toDouble")
	public CandyObject toDouble(VM vm, CandyObject[] args){
		return DoubleObj.valueOf(Double.valueOf(value));
	}
}
