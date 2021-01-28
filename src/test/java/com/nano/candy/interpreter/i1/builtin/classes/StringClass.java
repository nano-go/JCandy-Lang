package com.nano.candy.interpreter.i1.builtin.classes;

import com.nano.candy.interpreter.error.InitializetionError;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;

public class StringClass extends CandyClass {
	
	public static final StringClass STRING_CLASS = new StringClass();
	
	public StringClass() {
		super("String", ObjectClass.getInstance());
		makeUninheritable();
	}

	@Override
	public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
		InitializetionError.throwUnsupportedinitialization(getClassName());
		return null;
	}
}
