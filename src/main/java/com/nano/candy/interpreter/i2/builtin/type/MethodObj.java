package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.vm.VM;

/**
 * The MethodObj bound with an object.
 */
public class MethodObj extends CallableObj {
	
	// the 'method' always takes an extra parameter representing
	// the 'this' pointer.
	private static ParametersInfo genParametersInfo(CallableObj method) {
		int varargsIndex = method.varArgsIndex();
		varargsIndex = varargsIndex < 0 ? varargsIndex : varargsIndex-1;
		return new ParametersInfo(method.arity()-1, varargsIndex);
	}
	
	private CandyObject receiver;
	private CallableObj method;
	
	public MethodObj(CandyObject receiver, CallableObj method) {
		super(
			method.declaredName(), method.fullName(), 
			genParametersInfo(method)
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
