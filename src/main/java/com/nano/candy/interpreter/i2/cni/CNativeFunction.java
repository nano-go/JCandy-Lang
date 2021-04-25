package com.nano.candy.interpreter.i2.cni;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.vm.VM;
import java.lang.reflect.Method;

public class CNativeFunction extends CNativeCallable {
	
	private Method method;
	protected CNativeFunction(String name, int arity, Method method) {
		super(name, name, arity);
		this.method = method;
	}
	
	public Method getMethod() {
		return method;
	}
	
	@Override
	protected CandyObject onCall(VM vm, CandyObject instance, CandyObject[] args) throws Exception {
		return (CandyObject) method.invoke(null, vm, args);
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
