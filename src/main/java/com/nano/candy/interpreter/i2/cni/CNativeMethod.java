package com.nano.candy.interpreter.i2.cni;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.vm.VM;
import java.lang.reflect.Method;

public class CNativeMethod extends CNativeCallable {
	
	private Method method;
	private int arity;

	protected CNativeMethod(String simpleName, String name,
	                        int arity, int vaargIndex, Method method) {
		// extra hidden instance argument.
		super(simpleName, name, arity + 1, vaargIndex);
		this.method = method;
		this.arity = arity;
	}
	
	public Method getMethod() {
		return method;
	}

	@Override
	protected CandyObject onCall(VM vm, CandyObject instance, CandyObject[] args) throws Exception {
		return (CandyObject) method.invoke(instance, vm, args);
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
