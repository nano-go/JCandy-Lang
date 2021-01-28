package com.nano.candy.interpreter.i1.builtin.classes;

import com.nano.candy.interpreter.error.InitializetionError;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;

public class CallableClass extends CandyClass { 

	private static CallableClass instance;
	
	public static CallableClass getInstance() {
		if (instance == null) {
			instance = new CallableClass();
		}
		return instance;
	}

	private CallableClass() {
		super("Callable", ObjectClass.getInstance());
		makeUninheritable();
	}

	@Override
	public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
		InitializetionError.throwUnsupportedinitialization(getClassName());
		return null;
	}    
}
