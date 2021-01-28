package com.nano.candy.interpreter.i1.builtin.type;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.classes.NumberClass;

public class DoubleObject extends NumberObject {

	private double number;

	public DoubleObject(double number) {
		super(NumberClass.DOUBLE_CLASS);
		this.number = number;
	}

	@Override
	public CandyObject negative() {
		return new DoubleObject(- number);
	}

	@Override
	public CandyObject positive() {
		return this;
	}

	@Override
	public boolean isUpperCastClass() {
		return true;
	}

	@Override
	public long intValue() {
		return (long) number;
	}

	@Override
	public double doubleValue() {
		return number;
	}
	
}
