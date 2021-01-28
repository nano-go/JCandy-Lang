package com.nano.candy.interpreter.i1.builtin.classes;

import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;
import com.nano.candy.interpreter.i1.builtin.type.NullPointer;

public class NullPointerClass extends CandyClass {

	public static final NullPointerClass NULL_POINTER_CLASS = new NullPointerClass();

	public NullPointerClass() {
		super("NullPointer", ObjectClass.getInstance());
		makeUninheritable();
	}

	@Override
	public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
		return NullPointer.nil();
	}    
}
