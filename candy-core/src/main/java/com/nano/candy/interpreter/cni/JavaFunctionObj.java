package com.nano.candy.interpreter.cni;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.runtime.OperandStack;

public class JavaFunctionObj extends CallableObj {
	
	@FunctionalInterface
	public static interface Callback {
		public CandyObject onCall(CNIEnv env, CandyObject[] args);
	}
	
	private Callback callback;
	public JavaFunctionObj(String name,
	                          int arity, Callback callback) {
		this(name, arity, -1, callback);
	}
	
	public JavaFunctionObj(String name,
	                       int arity,
						   int varArgsIndex, Callback callback) {
		super(name, name, new ParametersInfo(arity, varArgsIndex));
		this.callback = callback;
	}
	
	public JavaFunctionObj(String className, String name,
	                          int arity, Callback callback) {
		this(className, name, arity, -1, callback);
	}
	
	public JavaFunctionObj(String className, String name,
	                       int arity, int varArgsIndex,
	                       Callback callback) {
		super(name, ObjectHelper.methodName(className, name), 
			new ParametersInfo(arity, varArgsIndex)
		);
		this.callback = callback;
	}

	@Override
	public void onCall(CNIEnv env, OperandStack opStack, int argc, int unpackFlags) {
		CandyObject retVal;
		if (argc == 0) {
			retVal = callback.onCall(env, ArrayObj.EMPTY_ARRAY);
		} else {
			CandyObject[] args = new CandyObject[argc];
			for (int i = argc - 1; i >= 0; i --) {
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
		return fullName.equals(funcName) ? "function" : "method";
	}
}
