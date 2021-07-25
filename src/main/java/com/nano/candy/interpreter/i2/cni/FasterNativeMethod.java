package com.nano.candy.interpreter.i2.cni;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.runtime.OperandStack;
import com.nano.candy.interpreter.i2.runtime.StackFrame;

/**
 * Faster than the native method implemented by the reflection.
 */
public class FasterNativeMethod extends CallableObj {

	@FunctionalInterface
	public static interface Callback {
		public CandyObject onCall(CNIEnv env, CandyObject[] args);
	}
	
	private Callback callback;
	
	public FasterNativeMethod(String className, String name,
	                          int arity, Callback callback) {
		this(className, name, arity, -1, callback);
	}
	
	public FasterNativeMethod(String className, String name,
	                          int arity, int varArgsIndex,
	                          Callback callback) {
		super(name, ObjectHelper.methodName(className, name), 
			new ParametersInfo(arity, varArgsIndex)
		);
		this.callback = callback;
	}

	@Override
	public void onCall(CNIEnv env, OperandStack opStack, StackFrame stack, int argc, int unpackFlags) {
		CandyObject retVal;
		if (argc == 0) {
			retVal = callback.onCall(env, ArrayObj.EMPTY_ARRAY);
		} else {
			CandyObject[] args = new CandyObject[argc];
			args = new CandyObject[argc];
			for (int i = 0; i < argc; i ++) {
				args[i] = opStack.pop();
			}
			retVal = callback.onCall(env, args);
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
