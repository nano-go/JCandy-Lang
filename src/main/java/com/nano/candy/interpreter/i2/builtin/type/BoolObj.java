package com.nano.candy.interpreter.i2.builtin.type;
import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.StringFunctions;

@BuiltinClass("Bool")
public class BoolObj extends BuiltinObject {

	public static final CandyClass BOOL_CLASS = BuiltinClassFactory.generate(BoolObj.class);
	
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
