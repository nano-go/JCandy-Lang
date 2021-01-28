package com.nano.candy.interpreter.i1.builtin.classes;

import com.nano.candy.interpreter.error.InitializetionError;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;

public class BooleanClass extends CandyClass {
	
	public static final BooleanClass BOOL_CLASS = new BooleanClass();
	
	public BooleanClass() {
		super("Bool", ObjectClass.getInstance());
		makeUninheritable();
	}
	
	@Override
	public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
		InitializetionError.throwUnsupportedinitialization(getClassName());
		return null;
	}
}
