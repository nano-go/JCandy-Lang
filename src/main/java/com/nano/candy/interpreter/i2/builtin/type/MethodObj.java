package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import com.nano.candy.interpreter.i2.runtime.OperandStack;
import com.nano.candy.interpreter.i2.runtime.StackFrame;

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
	public void onCall(CNIEnv env, OperandStack opStack, StackFrame stack, int argc, int unpackFlags) {
		opStack.push(receiver);
		method.onCall(env, opStack, stack, argc + 1, unpackFlags << 1);
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
