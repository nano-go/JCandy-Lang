package com.nano.candy.interpreter.i2.cni;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.vm.CarrierErrorException;
import com.nano.candy.interpreter.i2.vm.VM;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CNativeFunction extends CallableObj {
	
	private static final CandyObject[] EMPTY_ARGS = new CandyObject[0];
	
	private Method method;
	protected CNativeFunction(String name, int arity, Method method) {
		super(name, arity);
		this.method = method;
	}
	
	public Method getMethod() {
		return method;
	}
	
	@Override
	public void onCall(VM vm) {
		CandyObject[] args;
		if (arity == 0) {
			args = EMPTY_ARGS ;
		} else {
			args = new CandyObject[arity]; 
			for (int i = 0; i < arity; i ++) {
				args[i] = vm.pop();
			}
		}
		try {
			CandyObject ret = (CandyObject) method.invoke(null, vm, args);
			if (ret == null) {
				ret = NullPointer.nil();
			}
			vm.returnFromVM(ret);
		} catch (CarrierErrorException e){
			throw e;
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			if (t instanceof CarrierErrorException) {
				throw (CarrierErrorException) t;
			}
			new NativeError(t).throwSelfNative();
		} catch (Exception e) {
			new NativeError(e).throwSelfNative();
		}
	}

	@Override
	public boolean isBuiltin() {
		return true;
	}

	@Override
	protected String toStringTag() {
		return "native function";
	}
}
