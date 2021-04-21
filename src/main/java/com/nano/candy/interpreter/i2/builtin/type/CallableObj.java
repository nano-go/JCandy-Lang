package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.vm.VM;

@NativeClass(name = "Callable")
public abstract class CallableObj extends BuiltinObject {

	public static final CandyClass CALLABLE_CLASS = 
		NativeClassRegister.generateNativeClass(CallableObj.class);
	
	protected String declredName;
	protected String name;
	protected int arity;
	
	public CallableObj(String name, int arity) {
		this(name, name, arity);
	}
	
	public CallableObj(String declredName, String name, int arity) {
		super(CALLABLE_CLASS);
		this.declredName = declredName;
		this.name = name;
		this.arity = arity;
	}
	
	public final String declredName() {
		return declredName;
	}
	
	public final String name() {
		return name;
	}
	
	@Override
	public int arity() {
		return arity;
	}
	
	@Override
	public final boolean isCallable() {
		return true;
	}

	@Override
	public String toString() {
		return ObjectHelper.toString(toStringTag(), "%s(%d)", name, arity);
	}
	
	@NativeMethod(name = "arity")
	public final CandyObject arity(VM vm, CandyObject[] args) {
		return IntegerObj.valueOf(arity());
	}
	
	@NativeMethod(name = "name")
	public final CandyObject name(VM vm, CandyObject[] args) {
		return StringObj.valueOf(declredName);
	}
	
	@Override
	public abstract void onCall(VM vm);
	
	public abstract boolean isBuiltin();
	
	/**
	 * The tag is used in {@link #toString()}
	 */
	protected abstract String toStringTag();
}
