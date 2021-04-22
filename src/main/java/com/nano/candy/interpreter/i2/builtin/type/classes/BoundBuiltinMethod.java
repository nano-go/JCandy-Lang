package com.nano.candy.interpreter.i2.builtin.type.classes;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.vm.VM;

/**
 * This is a built-in method bound with an instance.
 *
 * It is easy and portable to create a built-in method by the 
 * java 8 functional feature, Method-Reference.
 */
public class BoundBuiltinMethod extends CallableObj {

	private CandyObject receiver;
	private MethodCallback callback;
	
	/**
	 * For example:
	 * <pre><code>
	 * public static CandyObject xxx(CandyObject instance, VM vm) {...}
	 * ...
	 * return new BoundBuiltinMethod(this, xxx, 2, ClassName::xxx);
	 * </code></pre>
	 *
	 * @param receiver the instance bound with this.
	 * @param name     the name of this method.
	 * @param arity    the arity of this method (excluding self). 
	 */
	public BoundBuiltinMethod(CandyObject receiver, String name, 
	                          int arity, MethodCallback callback) {
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
