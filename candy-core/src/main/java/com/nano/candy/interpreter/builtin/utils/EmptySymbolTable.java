package com.nano.candy.interpreter.builtin.utils;
import com.nano.candy.interpreter.builtin.CandyObject;

public class EmptySymbolTable extends SymbolTable {
	
	@Override
	public final CandyObject put(String name, CandyObject value) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final CandyObject putWithModfiers(String name, CandyObject value, byte modifiers) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void ensureEnoughTableSize(int minimumSize) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final CandyObject remove(String name) {
		return null;
	}

	@Override
	public final CandyObject get(String name) {
		return null;
	}

	@Override
	public final ObjAttribute getAttr(String name) {
		return null;
	}

	@Override
	public final int size() {
		return 0;
	}
}
