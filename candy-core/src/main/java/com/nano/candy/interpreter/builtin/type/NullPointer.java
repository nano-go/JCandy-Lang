package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.std.StringFunctions;

@NativeClass(name = "NullPointer")
public class NullPointer extends CandyObject {

	public static final CandyClass NULL_POINTER_CLASS =
		NativeClassRegister.generateNativeClass(NullPointer.class);
	
	private static NullPointer nil = new NullPointer() ;

	public static NullPointer nil() {
		return nil ;
	}

	private NullPointer() {
		super(NULL_POINTER_CLASS);
	}

	@Override
	public BoolObj boolValue(CNIEnv env) {
		return BoolObj.FALSE;
	}

	@Override
	public BoolObj equals(CNIEnv env, CandyObject obj) {
		return BoolObj.valueOf(obj == this);
	}

	@Override
	public String toString() {
		return StringFunctions.nullStr();
	}
}


