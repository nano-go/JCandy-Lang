package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.StringFunctions;

@BuiltinClass("NullPointer")
public class NullPointer extends BuiltinObject {

	public static final CandyClass NULL_POINTER_CLASS = BuiltinClassFactory.generate(NullPointer.class);
	
	private static NullPointer nil = new NullPointer() ;

	public static NullPointer nil() {
		return nil ;
	}

	private NullPointer() {
		super(NULL_POINTER_CLASS);
	}

	@Override
	public BoolObj boolValue(VM vm) {
		return BoolObj.FALSE;
	}

	@Override
	public BoolObj equals(VM vm, CandyObject obj) {
		return BoolObj.valueOf(obj == this);
	}

	@Override
	public String toString() {
		return StringFunctions.nullStr();
	}
}


