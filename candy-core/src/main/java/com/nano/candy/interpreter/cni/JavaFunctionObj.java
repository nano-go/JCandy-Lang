package com.nano.candy.interpreter.cni;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.runtime.OperandStack;

public class JavaFunctionObj extends CallableObj {
	
	@FunctionalInterface
	public static interface Callback {
		public CandyObject onCall(CNIEnv env, OperandStack opStack);
	}
	
	private Callback callback;
	public JavaFunctionObj(String name,
	                       int arity, Callback callback) {
		this(name, arity, -1, callback);
	}
	
	public JavaFunctionObj(String name,
	                       int arity,
						   int varArgsIndex, Callback callback) {
		super(name, name, arity, varArgsIndex);
		this.callback = callback;
	}
	
	public JavaFunctionObj(String className, String name,
	                       int arity, Callback callback) {
		this(className, name, arity, -1, callback);
	}
	
	public JavaFunctionObj(String className, String name,
	                       int arity, int varArgsIndex,
	                       Callback callback) {
		super(
			name, ObjectHelper.methodName(className, name), 
			arity, varArgsIndex);
		this.callback = callback;
	}

	@Override
	public void onCall(CNIEnv env, OperandStack opStack, int argc, int unpackFlags) {
		CandyObject retVal = callback.onCall(env, opStack);
		opStack.push(retVal != null ? retVal : NullPointer.nil());
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
