package com.nano.candy.interpreter.i1.builtin.classes;

import com.nano.candy.interpreter.error.InitializetionError;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;
import com.nano.candy.interpreter.i1.builtin.type.NumberObject;

public class NumberClass extends BuiltinClassImpl {

	public static final NumberClass NUMBER_CLASS = new NumberClass();
	public static final DoubleClass DOUBLE_CLASS = new DoubleClass();
	public static final IntegerClass INTEGER_CLASS = new IntegerClass();
	
	public NumberClass() {
		super(NumberObject.class, "Number");
	}

	@Override
	public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
		InitializetionError.throwUnsupportedinitialization(getClassName());
		return null;
	}
	
	public static class DoubleClass extends CandyClass {
		
		public DoubleClass() {
			super("Double", NUMBER_CLASS);
			makeUninheritable();
		}

		@Override
		public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
			InitializetionError.throwUnsupportedinitialization(getClassName());
			return null;
		}
	}
	
	public static class IntegerClass extends CandyClass {

		public IntegerClass() {
			super("Integer", NUMBER_CLASS);
			makeUninheritable();
		}

		@Override
		public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
			InitializetionError.throwUnsupportedinitialization(getClassName());
			return null;
		}
	}
}
