package com.nano.candy.interpreter.cni;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.interpreter.builtin.type.error.NativeError;
import com.nano.candy.interpreter.runtime.CarrierErrorException;
import com.nano.candy.interpreter.runtime.ContinueRunException;
import com.nano.candy.interpreter.runtime.OperandStack;
import com.nano.candy.interpreter.runtime.FrameStack;
import com.nano.candy.interpreter.runtime.VMExitException;
import java.lang.reflect.InvocationTargetException;

@NativeClass(name = "NativeCallable")
public abstract class CNativeCallable extends CallableObj {
	
	private static final CandyObject[] EMPTY_ARGUMENTS = new CandyObject[0];
	
	public CNativeCallable(String declaredName, String name,
	                       int arity, int varArgsIndex) {
		super(
			declaredName, name,
			new ParametersInfo(arity, varArgsIndex)
		);
	}

	@Override
	public void onCall(CNIEnv env, OperandStack opStack,
					   int argc, int unpackFlags) {
		CandyObject instance = null;
		if (isMethod()) {
			instance = opStack.pop();
			argc --;
		}
		CandyObject[] args = EMPTY_ARGUMENTS;
		if (argc != 0) {
			args = new CandyObject[argc];
			for (int i = argc - 1 ; i >= 0; i --) {
				args[i] = opStack.pop();
			}
		}
		try {
			CandyObject ret = onCall(env, instance, args);
			if (ret == null) {
				ret = NullPointer.nil();
			}
			opStack.push(ret);
		} catch (CarrierErrorException | VMExitException | ContinueRunException e){
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
	
	protected abstract CandyObject onCall(CNIEnv env,	         
	                                      CandyObject instance,
										  CandyObject[] args) throws Exception;
	public abstract boolean isMethod();
}
