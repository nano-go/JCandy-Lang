package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.builtin.type.error.RangeError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.builtin.utils.IndexHelper;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.builtin.utils.OptionalArg;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeMethod;
import com.nano.candy.std.Names;
import com.nano.candy.std.StringFunctions;
import com.nano.candy.utils.ArrayUtils;
import java.util.ArrayList;

@NativeClass(name = "String", isInheritable=true)
public class StringObj extends CandyObject {

	public static final CandyClass STRING_CLASS =
		NativeClassRegister.generateNativeClass(StringObj.class);
	
	public static final StringObj EMPTY_STR = new StringObj("");
	public static final StringObj RECURSIVE_LIST = new StringObj("[...]");
	public static final StringObj EMPTY_LIST = new StringObj("[]");
	public static final StringObj EMPTY_TUPLE = new StringObj("()");
	
	public static StringObj[] CHARS = null;
	
	public static void initChars() {
		CHARS = new StringObj[128];
		for (int i = 0; i < CHARS.length; i ++) {
			CHARS[i] = new StringObj(String.valueOf((char)i));
		}
	}
	
	public static StringObj valueOf(String val) {
		if (val.length() == 1) {
			return valueOf(val.charAt(0));
		}
		return new StringObj(val);
	}
	
	public static StringObj valueOf(char ch) {
		if (ch < 128) {
			if (CHARS == null) initChars();
			return CHARS[ch];
		}
		return new StringObj(String.valueOf(ch));
	}
	
	private String value;
	private IntegerObj hashCode;
	
	protected StringObj() {
		super(STRING_CLASS);
	}
	
	public StringObj(String value) {
		super(STRING_CLASS);
		this.value = value;
	}
	
	public String value() {
		return value;
	}

	@Override
	public CandyObject iterator(CNIEnv env) {
		return new IteratorObj.StringIterator(this.value);
	}

	@Override
	protected BoolObj lt(CNIEnv env, CandyObject operand) {
		if (operand instanceof StringObj) {
			return BoolObj.valueOf(
				value.compareTo(((StringObj)operand).value) < 0
			);
		}
		return super.lt(env, operand);
	}

	@Override
	protected BoolObj lteq(CNIEnv env, CandyObject operand) {
		if (operand instanceof StringObj) {
			return BoolObj.valueOf(
				value.compareTo(((StringObj)operand).value) <= 0
			);
		}
		return super.lteq(env, operand);
	}

	@Override
	protected BoolObj gt(CNIEnv env, CandyObject operand) {
		if (operand instanceof StringObj) {
			return BoolObj.valueOf(
				value.compareTo(((StringObj)operand).value) > 0
			);
		}
		return super.gt(env, operand);
	}

	@Override
	protected BoolObj gteq(CNIEnv env, CandyObject operand) {
		if (operand instanceof StringObj) {
			return BoolObj.valueOf(
				value.compareTo(((StringObj)operand).value) >= 0
			);
		}
		return super.gteq(env, operand);
	}
	
	@Override
	public CandyObject add(CNIEnv env, CandyObject operand) {
		StringObj str = operand.callStr(env);
		return valueOf(value + str.value);
	}

	@Override
	public CandyObject mul(CNIEnv env, CandyObject operand) {
		if (operand instanceof IntegerObj) {
			long c = ((IntegerObj) operand).value;
			return valueOf(StringFunctions.repeat(value, c));
		}
		return super.mul(env, operand);
	}

	@Override
	protected CandyObject getItem(CNIEnv env, CandyObject key) {
		if (key instanceof Range) {
			Range range = (Range) key;
			int begin = IndexHelper.asIndex(range.getLeftObj(), value.length());
			int end = IndexHelper.asIndexForAdd(range.getRightObj(), value.length());
			return end <= begin ? EMPTY_STR : 
				new StringObj(value.substring(begin, end));
		}
		int index = IndexHelper.asIndex(key, value.length());
		return valueOf(value.charAt(index));
	}

	@Override
	public BoolObj equals(CNIEnv env, CandyObject operand) {
		if (operand == this) {
			return BoolObj.TRUE;
		}
		if (operand instanceof StringObj) {
			String str = ((StringObj)operand).value();
			return BoolObj.valueOf(StringFunctions.equals(value, str));
		}
		return BoolObj.FALSE;
	}

	@Override
	public StringObj str(CNIEnv env) {
		return this;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public IntegerObj hashCode(CNIEnv env) {
		if (hashCode == null)
			hashCode = IntegerObj.valueOf(value.hashCode());
		return hashCode;
	}
	
	@NativeMethod(name = Names.METHOD_INITALIZER)
	protected CandyObject init(CNIEnv env, String value) {
		this.value = value;
		return this;
	}
	
	@NativeMethod(name = "substr")
	public CandyObject substring(CNIEnv env, long beginIndex, long endIndex) {
		final int size = value.length();
		beginIndex = RangeError.checkIndex(beginIndex, size);
		endIndex = RangeError.checkIndexForAdd(endIndex, size);
		if (beginIndex > endIndex) {
			return EMPTY_STR;
		}
		return valueOf(value.substring(
			(int) beginIndex, (int) endIndex));
	}
	
	@NativeMethod(name = "length")
	public CandyObject length(CNIEnv env) {
		return IntegerObj.valueOf(value.length());
	}
	
	@NativeMethod(name = "startWith")
	public CandyObject startWith(CNIEnv env, CandyObject prefixObj) {
		String prefix = prefixObj.callStr(env).value;
		return BoolObj.valueOf(value.startsWith(prefix));
	}
	
	@NativeMethod(name = "endWith")
	public CandyObject endWith(CNIEnv env, CandyObject suffxObj) {
		String suffix = suffxObj.callStr(env).value;
		return BoolObj.valueOf(value.endsWith(suffix));
	}
	
	@NativeMethod(name = "split")
	public CandyObject split(CNIEnv env, StringObj regex) {
		String[] res = value.split(regex.value);
		CandyObject[] elements = new CandyObject[res.length];
		for (int i = 0; i < res.length; i ++) {
			elements[i] = valueOf(res[i]);
		}
		return new ArrayObj(elements);
	}
	
	@NativeMethod(name = "splitlines")
	public CandyObject splitlines(CNIEnv env) {
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
	public CandyObject trim(CNIEnv env) {
		return valueOf(value.trim());
	}
	
	@NativeMethod(name = "replaceFirst")
	public CandyObject replace(CNIEnv env, StringObj regex, String replacement) {
		return valueOf(value.replaceFirst(regex.value, replacement));
	}
	

	@NativeMethod(name = "replaceAll")
	public CandyObject replaceAll(CNIEnv env, StringObj regex, String replacement) {
		return valueOf(value.replaceAll(regex.value, replacement));
	}
	
	@NativeMethod(name = "join")
	public CandyObject join(CNIEnv env, ArrayObj array) {
		final int size = array.length();
		if (size == 0) {
			return EMPTY_STR;
		}
		StringBuilder builder = new StringBuilder();
		int i = 0;
		int iMax = size-1;
		for (;;) {
			builder.append(array.get(i).callStr(env).value);
			if (i == iMax) {
				break;
			}
			i ++;
			builder.append(this.value);
		}
		return valueOf(builder.toString());
	}
	
	@NativeMethod(name = "lastIndexOf") 
	public CandyObject lastIndexOf(CNIEnv env, StringObj str){
		return IntegerObj.valueOf(value.lastIndexOf(str.value));
	}
	
	@NativeMethod(name = "indexOf") 
	public CandyObject indexOf(CNIEnv env, StringObj str){
		return IntegerObj.valueOf(value.indexOf(str.value));
	}
	
	@NativeMethod(name = "codePoint")
	public CandyObject toCodePoint(CNIEnv env){
		if (value.length() != 1) {
			new TypeError("The object is not a single char: <%s>", value)
				.throwSelfNative();
		}
		return IntegerObj.valueOf(value.charAt(0));
	}
	
	@NativeMethod(name = "toUpperCase") 
	public CandyObject toUpperCase(CNIEnv env){
		return valueOf(value.toUpperCase());
	}
	
	@NativeMethod(name = "toLowerCase") 
	public CandyObject toLowerCase(CNIEnv env){
		return valueOf(value.toLowerCase());
	}
	
	@NativeMethod(name = "capitalize")
	public CandyObject capitalize(CNIEnv env){
		return valueOf(
			value.substring(0, 1).toUpperCase().concat(
				value.substring(1)
			)
		);
	}
	
	@NativeMethod(name = "toInt")
	public CandyObject toInt(CNIEnv env, OptionalArg radixArg){
		int radix = (int) ObjectHelper.asInteger(radixArg.getValue(10));
		return IntegerObj.valueOf(Integer.valueOf(value, radix));
	}
	
	@NativeMethod(name = "toDouble")
	public CandyObject toDouble(CNIEnv env){
		return DoubleObj.valueOf(Double.valueOf(value));
	}
}
