package com.nano.candy.interpreter.i2.cni;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.nano.candy.interpreter.i2.builtin.CandyObject;

public class CNativeMethod extends CNativeCallable {
	
	private MethodAccess method;
	private int index;

	protected CNativeMethod(String simpleName, String name,
	                        int arity, int varArgsIndex, 
	                        MethodAccess method, int index) {
		// extra hidden instance qargument.
		super(
			simpleName, name, arity + 1, 
			varArgsIndex == -1 ? -1 : varArgsIndex + 1
		);
		this.method = method;
		this.index = index;
	}

	@Override
	protected CandyObject onCall(CNIEnv env, CandyObject instance, CandyObject[] args) throws Exception {
		return (CandyObject) method.invoke(instance, index, env, args);
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
