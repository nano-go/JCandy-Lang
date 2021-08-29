package com.nano.candy.interpreter.builtin.utils;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.cni.CNIEnv;
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
