package com.nano.candy.interpreter.i2.rtda;
import com.nano.candy.interpreter.i2.builtin.CandyObject;

public class Variable implements Comparable<Variable> {

	private String name;
	private CandyObject value;
	
	protected static Variable getVariable(String name, CandyObject value) {
		return new Variable(name, value);
	}
	
	private Variable(String name, CandyObject value) {
		this.name = name;
		this.value = value;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	protected void setValue(CandyObject value) {
		this.value = value;
	}

	public CandyObject getValue() {
		return value;
	}

	@Override
	public int compareTo(Variable variable) {
		return this.name.compareTo(variable.name);
	}
}
