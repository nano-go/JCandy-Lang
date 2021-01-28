package com.nano.candy.interpreter.i1.builtin.type;

import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.classes.NumberClass;

public class IntegerObject extends NumberObject {

	private long number ;

	public IntegerObject(long number) {
		super(NumberClass.INTEGER_CLASS);
		this.number = number;
	}

	@Override
	public CandyObject negative() {
		return new IntegerObject(- number);
	}

	@Override
	public CandyObject positive() {
		return this;
	}

	@Override
	public StringObject stringValue() {
		return StringObject.of(String.valueOf(intValue()));
	}

	@Override
	public long intValue() {
		return number;
	}

	@Override
	public double doubleValue() {
		return number;
	}

	@Override
	public boolean isUpperCastClass() {
		return false;
	}

}
