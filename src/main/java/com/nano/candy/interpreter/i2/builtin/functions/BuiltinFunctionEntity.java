package com.nano.candy.interpreter.i2.builtin.functions;

import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.vm.VM;

public class BuiltinFunctionEntity extends CallableObj {
	
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
