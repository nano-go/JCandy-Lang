package com.nano.candy.interpreter.i2.builtin.type.classes;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.vm.VM;

@FunctionalInterface
public interface MethodCallback {
	public CandyObject onCall(CandyObject instance, VM vm);
}
