package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.vm.VM;

@BuiltinClass("Callable")
public abstract class CallableObj extends BuiltinObject {

	public static final CandyClass CALLABLE_CLASS = BuiltinClassFactory.generate(CallableObj.class);
	
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
	
	@BuiltinMethod(name = "arity")
	public final void arity(VM vm) {
		vm.returnFromVM(IntegerObj.valueOf(arity()));
	}
	
	@BuiltinMethod(name = "name")
	public final void name(VM vm) {
		vm.returnFromVM(StringObj.valueOf(declredName));
	}
	
	@Override
	public abstract void onCall(VM vm);
	
	public abstract boolean isBuiltin();
	
	/**
	 * The tag is used in {@link #toString()}
	 */
	protected abstract String toStringTag();
}
