package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.vm.VM;

public class MethodObj extends CallableObj {

	private CandyObject receiver;
	private CallableObj method;
	
	// the 'method' always takes an extra parameter representing
	// the 'this' pointer.
	public MethodObj(CandyObject receiver, CallableObj method) {
		super(method.declredName(), method.name(), method.arity - 1);
		this.receiver = receiver;
		this.method = method;
	}

	@Override
	public boolean isBuiltin() {
		return method.isBuiltin();
	}
	
	@Override
	public void onCall(VM vm) {
		vm.frame().push(receiver);
		method.onCall(vm);
	}

	@Override
	protected String toStringTag() {
		return method.toStringTag();
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
