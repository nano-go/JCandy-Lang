package com.nano.candy.interpreter.builtin.type;
import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.IntegerObj;
import com.nano.candy.interpreter.builtin.type.NumberObj;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeMethod;
import com.nano.candy.std.Names;
import com.nano.candy.std.StringFunctions;

@NativeClass(name = "Integer", isInheritable=true)
public class IntegerObj extends NumberObj {
	
	public static final CandyClass INTEGER_CLASS = 
		NativeClassRegister.generateNativeClass(IntegerObj.class, NumberObj.NUMBER_CLASS);

	private static final IntegerObj[] CACHES = new IntegerObj[256];
	
	static {
		int halfS = CACHES.length/2;
		for (int i = -halfS; i < halfS; i ++) {
			CACHES[i + halfS] = new IntegerObj(i);
		}
	}
	
	public static IntegerObj valueOf(long val) {
		final int halfS = CACHES.length/2;
		if (val < halfS && val >= -halfS) {
			return CACHES[(int)(val+halfS)];
		}
		return new IntegerObj(val);
	}
	
	protected long value;
	
	protected IntegerObj() {
		super(INTEGER_CLASS);
	}
	
	private IntegerObj(long value) {
		super(INTEGER_CLASS);
		this.value = value;
	}
	
	@NativeMethod(name = Names.METHOD_INITALIZER, argc = 1)
	public CandyObject init(CNIEnv env, CandyObject[] args) {
		this.value = ObjectHelper.asInteger(args[0]);
		return this;
	}
	
	@NativeMethod(name = "toChar")
	public CandyObject toChar(CNIEnv env, CandyObject[] args) {
		return StringObj.valueOf((char) value);
	}

	@Override
	public CandyObject negative(CNIEnv env) {
		return valueOf(-value);
	}
	
	@Override
	protected boolean isDouble() {
		return false;
	}

	@Override
	public long intValue() {
		return value;
	}

	@Override
	public double doubleValue() {
		return value;
	}

	@Override
	public String toString() {
		return StringFunctions.valueOf(value);
	}

	@Override
	public IntegerObj hashCode(CNIEnv env) {
		return IntegerObj.valueOf(Long.hashCode(value));
	}
}
