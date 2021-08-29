package com.nano.candy.interpreter.builtin.type;
import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.DoubleObj;
import com.nano.candy.interpreter.builtin.type.NumberObj;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeMethod;
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
	public CandyObject init(CNIEnv env, CandyObject[] args) {
		this.value = ObjectHelper.asDouble(args[0]);
		return this;
	}

	@Override
	public CandyObject negative(CNIEnv env) {
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
	public IntegerObj hashCode(CNIEnv env) {
		return IntegerObj.valueOf(Double.hashCode(value));
	}
}
