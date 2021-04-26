package com.nano.candy.interpreter.i2.cni;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.vm.CarrierErrorException;
import com.nano.candy.interpreter.i2.vm.VM;
import java.lang.reflect.InvocationTargetException;

@NativeClass(name = "NativeCallable")
public abstract class CNativeCallable extends CallableObj {
	
	private static final CandyObject[] EMPTY_ARGUMENTS = new CandyObject[0];
	
	public static final CandyClass NATIVE_CALLABLE_CLASS
		= NativeClassRegister.generateNativeClass(CNativeCallable.class);
	
	public CNativeCallable(String declaredName, String name,
	                       int arity, int varArgsIndex) {
		super(
			declaredName, name,
			new ParametersInfo(arity, varArgsIndex)
		);
	}

	@Override
	protected void onCall(VM vm, int argc, int unpackFlags) {
		CandyObject instance = null;
		if (isMethod()) {
			instance = vm.pop();
			argc --;
		}
		CandyObject[] args = EMPTY_ARGUMENTS;
		if (argc != 0) {
			args = new CandyObject[argc];
			for (int i = 0; i < argc; i ++) {
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
