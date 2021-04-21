package com.nano.candy.interpreter.i2.cni;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.vm.CarrierErrorException;
import com.nano.candy.interpreter.i2.vm.VM;
import java.lang.reflect.InvocationTargetException;

public abstract class CNativeCallable extends CallableObj {

	private static final CandyObject[] EMPTY_ARGS = new CandyObject[0];
	
	public CNativeCallable(String declredName, String name, int arity) {
		super(declredName, name, arity);
	}
	
	@Override
	public void onCall(VM vm) {
		CandyObject instance = null;
		int arity = this.arity;
		if (isMethod()) {
			arity --;
			instance = vm.pop();
		}
		CandyObject[] args;
		if (arity == 0) {
			args = EMPTY_ARGS;
		} else {
			args = new CandyObject[arity]; 
			for (int i = 0; i < arity; i ++) {
				args[i] = vm.pop();
			}
		}
		try {
			CandyObject ret = onCall(vm, instance, args);
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
	public final boolean isBuiltin() {
		return true;
	}
	
	protected abstract CandyObject onCall(VM vm,
	                                      CandyObject instance,
										  CandyObject[] args) throws Exception;
	public abstract boolean isMethod();
}
