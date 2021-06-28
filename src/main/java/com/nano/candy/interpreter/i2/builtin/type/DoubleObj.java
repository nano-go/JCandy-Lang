package com.nano.candy.interpreter.i2.builtin.type;
import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.NumberObj;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.StringFunctions;

@NativeClass(name = "Double")
public class DoubleObj extends NumberObj {
    
	public static final CandyClass DOUBLE_CLASS = 
		NativeClassRegister.generateNativeClass(DoubleObj.class, NumberObj.NUMBER_CLASS);
	
	
    public static DoubleObj valueOf(double val) {
		return new DoubleObj(val);
	}
    
	double value;
	
	private DoubleObj(double value) {
		super(DOUBLE_CLASS);
		this.value = value;
	}

	@Override
	public CandyObject negative(VM vm) {
		return DoubleObj.valueOf(-value);
	}
	
	@Override
	public long intValue() {
		return (long) value;
	}

	@Override
	public double doubleValue() {
		return value;
	}

	@Override
	protected boolean isDouble() {
		return true;
	}

	@Override
	public String toString() {
		return StringFunctions.valueOf(value);
	}

	@Override
	public IntegerObj hashCode(VM vm) {
		return IntegerObj.valueOf(Double.hashCode(value));
	}
}
