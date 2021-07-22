package com.nano.candy.interpreter.i2.builtin.type;
import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.NumberObj;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import com.nano.candy.std.StringFunctions;

@NativeClass(name = "Double", isInheritable=true)
public class DoubleObj extends NumberObj {
    
	public static final CandyClass DOUBLE_CLASS = 
		NativeClassRegister.generateNativeClass(DoubleObj.class, NumberObj.NUMBER_CLASS);
	
    public static DoubleObj valueOf(double val) {
		return new DoubleObj(val);
	}
    
	double value;
	
	protected DoubleObj() {
		super(DOUBLE_CLASS);
	}
	
	private DoubleObj(double value) {
		super(DOUBLE_CLASS);
		this.value = value;
	}
	
	@NativeMethod(name = Names.METHOD_INITALIZER, argc = 1)
	public CandyObject init(VM vm, CandyObject[] args) {
		this.value = ObjectHelper.asDouble(args[0]);
		return this;
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
