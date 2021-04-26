package com.nano.candy.interpreter.i2.cni;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.vm.VM;

/**
 * Faster than the native method implemented by the reflection.
 */
public class FasterNativeMethod extends CallableObj {

	@FunctionalInterface
	public static interface Callback {
		public CandyObject onCall(VM vm, int argc);
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
	protected void onCall(VM vm, int argc, int unpackFlags) {
		vm.returnFromVM(callback.onCall(vm, argc));
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
