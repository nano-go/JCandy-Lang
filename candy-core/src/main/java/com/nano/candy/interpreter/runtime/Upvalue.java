package com.nano.candy.interpreter.runtime;

import com.nano.candy.interpreter.builtin.CandyObject;

public class Upvalue {
	
	private CandyObject[] slots;
	private int index;
	private CandyObject ref;

	public Upvalue(CandyObject[] slots, int index) {
		this.slots = slots;
		this.index = index;
		this.ref = slots[index];
	}
	
	public boolean isClosed() {
		return slots == null;
	}
	
	public void close() {
		this.ref = slots[index];
		slots = null;
	}
	
	public int index() {
		return index;
	}
	
	public void store(CandyObject ref) {
		this.ref = ref;
		if (slots != null) {
			slots[index] = ref;
		}
	}
	
	public CandyObject load() {
		if (slots != null) {
			return slots[index];
		}
		return ref;
	}
}
