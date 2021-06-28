package com.nano.candy.interpreter.i2.builtin.type;
import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.StringFunctions;

@NativeClass(name = "Bool")
public class BoolObj extends CandyObject {

	public static final CandyClass BOOL_CLASS =
		NativeClassRegister.generateNativeClass(BoolObj.class);
	
	public static final BoolObj TRUE = new BoolObj(true);
	public static final BoolObj FALSE = new BoolObj(false);
	
	public static BoolObj valueOf(boolean val) {
		return val ? TRUE : FALSE;
	}
	
	private boolean value;
	
	private BoolObj(boolean value) {
		super(BOOL_CLASS);
		this.value = value;
		freeze();
	}
	
	public boolean value() {
		return value;
	}

	@Override
	public BoolObj not(VM vm) {
		return valueOf(!value);
	}

	public BoolObj boolValue(VM vm) {
		return this;
	}
	
	@Override
	public String toString() {
		return StringFunctions.valueOf(value);
	}
}
