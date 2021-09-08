package com.nano.candy.interpreter.cni;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.interpreter.builtin.type.error.NativeError;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.runtime.OperandStack;

public class JavaMethodObj extends CallableObj {
	
	@FunctionalInterface
	public static interface Callback {
		public CandyObject onCall(CNIEnv env, CandyObject instance, OperandStack opStack);
	}
	
	private Callback callback;
	
	public JavaMethodObj(String className, String methodName, 
	                     int arity, int varArgsIndex, 
						 Callback callback) {
		super(
			methodName, ObjectHelper.methodName(className, methodName), 
			arity + 1, varArgsIndex);
		this.callback = callback;
	}
	
	@Override
	public void onCall(CNIEnv env, OperandStack opStack, int argc, int unpackFlags) {
		CandyObject retVal;
		CandyObject instance = opStack.pop();
		argc --;
		retVal = callback.onCall(env, instance, opStack);
		opStack.push(retVal != null ? retVal : NullPointer.nil());
	}

	@Override
	public boolean isBuiltin() {
		return true;
	}
	
	@Override
	protected String strTag() {
		return "method";
	}
}
