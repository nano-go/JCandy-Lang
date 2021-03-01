package com.nano.candy.interpreter.i2.builtin.type.classes;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.vm.VM;

/**
 * This is a bound built-in method which is a faster implementation way
 * of the bound method.
 *
 * You can simply create the method object by 
 * 'ObjectHelper#genMethod(CandyObject, String, int)' and the method reference
 * feature in Java8.
 */
public class BoundBuiltinMethod extends CallableObj {

	private CandyObject receiver;
	private MethodCallback callback;
	
	public BoundBuiltinMethod(CandyObject receiver, String name, int arity, MethodCallback callback) {
		super(name, ObjectHelper.methodName(
			receiver.getCandyClass(), name
		), arity);
		this.receiver = receiver;
		this.callback = callback;
	}
	
	@Override
	public void onCall(VM vm) {
		CandyObject obj = callback.onCall(receiver, vm);
		if (obj == null) {
			vm.returnNilFromVM();
		} else {
			vm.returnFromVM(obj);
		}
	}

	@Override
	public boolean isBuiltin() {
		return true;
	}

	@Override
	protected String toStringTag() {
		return "builtin-method";
	}
}
