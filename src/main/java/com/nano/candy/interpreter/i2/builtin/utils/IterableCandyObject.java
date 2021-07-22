package com.nano.candy.interpreter.i2.builtin.utils;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.vm.VM;
import java.util.Iterator;
import java.util.Spliterator;

public class IterableCandyObject implements Iterable<CandyObject> {

	private VM vm;
	private CandyObject obj;

	public IterableCandyObject(VM vm, CandyObject obj) {
		this.vm = vm;
		this.obj = obj;
	}
	
	@Override
	public Iterator<CandyObject> iterator() {
		return new CandyObjectIterator(vm, obj);
	}

	@Override
	public Spliterator<CandyObject> spliterator() {
		throw new UnsupportedOperationException();
	}
}
