package com.nano.candy.interpreter.i2.builtin.utils;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import java.util.Iterator;
import java.util.Spliterator;

public class IterableCandyObject implements Iterable<CandyObject> {

	private CNIEnv env;
	private CandyObject obj;

	public IterableCandyObject(CNIEnv env, CandyObject obj) {
		this.env = env;
		this.obj = obj;
	}
	
	@Override
	public Iterator<CandyObject> iterator() {
		return new CandyObjectIterator(env, obj);
	}

	@Override
	public Spliterator<CandyObject> spliterator() {
		throw new UnsupportedOperationException();
	}
}
