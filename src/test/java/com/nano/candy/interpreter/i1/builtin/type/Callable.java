package com.nano.candy.interpreter.i1.builtin.type;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;

public interface Callable {
	public int arity();
	public Callable bindToInstance(CandyObject instance);
	public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args);
}
