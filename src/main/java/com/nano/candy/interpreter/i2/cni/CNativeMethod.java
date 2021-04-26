package com.nano.candy.interpreter.i2.cni;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.vm.VM;

public class CNativeMethod extends CNativeCallable {
	
	private MethodAccess method;
	private int index;

	protected CNativeMethod(String simpleName, String name,
	                        int arity, int vaargIndex, 
	                        MethodAccess method, int index) {
		// extra hidden instance qargument.
		super(simpleName, name, arity + 1, vaargIndex);
		this.method = method;
		this.index = index;
	}

	@Override
	protected CandyObject onCall(VM vm, CandyObject instance, CandyObject[] args) throws Exception {
		return (CandyObject) method.invoke(instance, index, vm, args);
	}

	@Override
	public boolean isMethod() {
		return true;
	}

	@Override
	protected String strTag() {
		return "native method";
	}
}
