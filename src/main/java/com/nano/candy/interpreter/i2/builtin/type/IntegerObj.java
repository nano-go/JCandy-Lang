package com.nano.candy.interpreter.i2.builtin.type;
import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.NumberObj;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.StringFunctions;

@NativeClass(name = "Integer")
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
	
	private IntegerObj(long value) {
		super(INTEGER_CLASS);
		this.value = value;
	}

	@Override
	public CandyObject negative(VM vm) {
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
	public IntegerObj hashCode(VM vm) {
		return IntegerObj.valueOf(Long.hashCode(value));
	}
}
