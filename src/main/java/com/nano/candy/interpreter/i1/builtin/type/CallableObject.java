package com.nano.candy.interpreter.i1.builtin.type;

import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.classes.CallableClass;

public abstract class CallableObject extends CandyObject implements Callable { 

	protected int arity;
	
	public CallableObject(int arity) {
		this(arity, CallableClass.getInstance());
	}
	
	public CallableObject(int arity, CandyClass type) {
		super(type);
		this.arity = arity;
	}
	
	public int arity() {
		return arity;
	}
	
	public Callable bindToInstance(CandyObject instance) {
		throw new Error("");
	}
	
	public abstract CandyObject onCall(AstInterpreter interpreter, CandyObject[] args);

	@Override
	public StringObject stringValue() {
		return StringObject.of("<callable: paramaters(" + arity + ")>");
	}
}
