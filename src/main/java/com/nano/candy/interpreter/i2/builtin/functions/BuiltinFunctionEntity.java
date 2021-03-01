package com.nano.candy.interpreter.i2.builtin.functions;

import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.vm.VM;

@BuiltinClass("BuiltinFunction")
public class BuiltinFunctionEntity extends CallableObj {
	
	public static final CandyClass BUILTIN_FUNCTION_CLASS =
		BuiltinClassFactory.generate(BuiltinFunctionEntity.class, CALLABLE_CLASS);
	
	Callback callback;

	public BuiltinFunctionEntity(String name, int arity, Callback callback) {
		super(name, name, arity);
		this.callback = callback;
	}

	@Override
	public boolean isBuiltin() {
		return true;
	}
	
	@Override
	public void onCall(VM vm) {
		callback.onCall(vm);
	}

	@Override
	protected String toStringTag() {
		return "built-in function";
	}
}
