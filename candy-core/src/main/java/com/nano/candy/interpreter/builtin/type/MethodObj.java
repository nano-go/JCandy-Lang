package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.runtime.OperandStack;
import com.nano.candy.interpreter.runtime.FrameStack;
import java.util.Objects;

/**
 * The MethodObj bound with an object.
 */
public class MethodObj extends CallableObj {
	
	private CandyObject receiver;
	private CallableObj method;
	
	public MethodObj(CandyObject receiver, CallableObj method) {
		super(
			method.funcName(), method.fullName(),
			new ParametersInfo(method.arity()-1, method.vaargIndex())
		);
		this.receiver = receiver;
		this.method = method;
	}
	
	@Override
	public boolean isBuiltin() {
		return method.isBuiltin();
	}

	@Override
	public void onCall(CNIEnv env, OperandStack opStack, int argc, int unpackFlags) {
		opStack.push(receiver);
		method.onCall(env, opStack, argc + 1, unpackFlags);
	}

	@Override
	protected String strTag() {
		return "method";
	}

	@Override
	public int hashCode() {
		return Objects.hash(receiver, method);
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
