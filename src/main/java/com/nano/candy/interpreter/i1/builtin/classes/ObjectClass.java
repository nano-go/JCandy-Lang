package com.nano.candy.interpreter.i1.builtin.classes;

import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;

public class ObjectClass extends BuiltinClassImpl {
	private static CandyClass instance;
	
	public static CandyClass getInstance() {
		if (instance == null) {
			instance = new ObjectClass();
		}
		return instance;
	}
	
	private ObjectClass() {
		super(CandyObject.class, "Object", null);
	}
}
