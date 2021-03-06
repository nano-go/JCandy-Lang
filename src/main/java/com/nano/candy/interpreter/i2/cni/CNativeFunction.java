package com.nano.candy.interpreter.i2.cni;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.vm.VM;

public class CNativeFunction extends CNativeCallable {
	
	private MethodAccess method;
	private int index;
	
	protected CNativeFunction(String name, int arity, 
	                          int vaargIndex, 
	                          MethodAccess method, int index) {
		super(name, name, arity, vaargIndex);
		this.method = method;
		this.index = index;
	}
	
	@Override
	protected CandyObject onCall(VM vm, CandyObject instance, CandyObject[] args) throws Exception {
		return (CandyObject) method.invoke(null, index, vm, args);
	}

	@Override
	protected String strTag() {
		return "native function";
	}
	
	@Override
	public final boolean isMethod() {
		return false;
	}
}
