package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.vm.VM;

/**
 * A MethodObj binds an object and a method.
 */
public class MethodObj extends CallableObj {
	
	private CandyObject receiver;
	private CallableObj method;
	
	// the 'method' always takes an extra parameter representing
	// the 'this' pointer.
	public MethodObj(CandyObject receiver, CallableObj method) {
		super(
			method.declaredName(), method.fullName(), 
			new ParametersInfo(method.arity()-1, method.varArgsIndex()-1)
		);
		this.receiver = receiver;
		this.method = method;
	}
	
	@Override
	public boolean isBuiltin() {
		return method.isBuiltin();
	}

	@Override
	public void call(VM vm, int argc, int unpackingBits) {
		vm.push(receiver);
		method.call(vm, argc + 1, unpackingBits << 1);
	}

	@Override
	protected void onCall(VM vm, int argc, int unpackingBits) {
		throw new Error("Unsupported.");
	}

	@Override
	protected String strTag() {
		return "method";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodObj) {
			MethodObj m = (MethodObj) obj;
			return m.receiver == receiver &&
				m.method == method;
		}
		return false;
	}
}
