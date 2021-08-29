package com.nano.candy.interpreter.builtin.utils;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.cni.CNIEnv;
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
		return hasNext.call(env).boolValue(env).value();
	}

	@Override
	public CandyObject next() {
		return next.call(env);
	}
}
