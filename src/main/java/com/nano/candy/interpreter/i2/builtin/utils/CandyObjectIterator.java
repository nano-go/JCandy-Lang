package com.nano.candy.interpreter.i2.builtin.utils;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import com.nano.candy.std.Names;
import java.util.Iterator;

/**
 * Help to iterate the iterable Candy object.
 */
public class CandyObjectIterator implements Iterator<CandyObject> {

	private CallableObj hasNext;
	private CallableObj next;

	private CNIEnv env;

	/**
	 * Throw TypeError if the given object is not iterable.
	 */
	public CandyObjectIterator(CNIEnv env, CandyObject obj) {
		CandyObject iterator = obj.callIterator(env);
		CandyObject hasNext =
			iterator.callGetAttr(env, Names.METHOD_ITERATOR_HAS_NEXT);
		CandyObject next =
			iterator.callGetAttr(env, Names.METHOD_ITERATOR_NEXT);
		TypeError.checkIsCallable(hasNext);
		TypeError.checkIsCallable(next);
		this.hasNext = (CallableObj) hasNext;
		this.next = (CallableObj) next;
		this.env = env;
	}
	
	@Override
	public boolean hasNext() {
		return hasNext.callExeUser(env).boolValue(env).value();
	}

	@Override
	public CandyObject next() {
		return next.callExeUser(env);
	}
}
