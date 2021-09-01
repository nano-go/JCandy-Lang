package com.nano.candy.interpreter.cni;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.runtime.OperandStack;

public class JavaMethodObj extends CallableObj {
	
	@FunctionalInterface
	public static interface Callback {
		public CandyObject onCall(CNIEnv env, CandyObject instance, CandyObject[] args);
	}
	
	private Callback callback;
	
	public JavaMethodObj(String className, String methodName, 
	                     int arity, int varArgsIndex, 
						 Callback callback) {
		super(methodName, ObjectHelper.methodName(className, methodName),
			new ParametersInfo(arity + 1, varArgsIndex));
		this.callback = callback;
	}
	
	@Override
	public void onCall(CNIEnv env, OperandStack opStack, int argc, int unpackFlags) {
		CandyObject retVal;
		CandyObject instance = opStack.pop();
		argc --;
		if (argc == 0) {
			retVal = callback.onCall(env, instance, ArrayObj.EMPTY_ARRAY);
		} else {
			CandyObject[] args = new CandyObject[argc];
			for (int i = argc - 1; i >= 0; i --) {
				args[i] = opStack.pop();
			}
			retVal = callback.onCall(env, instance, args);
		}
		if (retVal == null) {
			retVal = NullPointer.nil();
		}
		opStack.push(retVal);
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
