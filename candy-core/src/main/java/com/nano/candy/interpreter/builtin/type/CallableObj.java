package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.code.ConstantValue;
import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.runtime.OperandStack;
import com.nano.candy.interpreter.runtime.FrameStack;

/**
 * Superclass of all functions.
 */
@NativeClass(name = "Callable")
public abstract class CallableObj extends CandyObject {
	
	public static final byte EMPTY_UNPACK_FLAGS = 0;
	
	private static CandyClass CALLABLE_CLASS = null;
	public static CandyClass getCallableClass() {
		if (CALLABLE_CLASS == null) {
			CALLABLE_CLASS = 
				NativeClassRegister.generateNativeClass(CallableObj.class);
		}
		return CALLABLE_CLASS;
	}
	
	public static class ParametersInfo {	
		/**
		 * The number of arguments.
		 */
		private final int arity;

		/**
		 * The parameter that this index is entry can accept zero or 
		 * more arguments.
		 *
		 * If this value is -1, it means no such parameters.
		 */
		private final int varArgsIndex;

		public ParametersInfo(int arity, int varArgsIndex) {
			this.arity = arity;
			this.varArgsIndex = varArgsIndex;
		}
		
		public ParametersInfo(ConstantValue.MethodInfo info) {
			this.arity = info.arity;
			this.varArgsIndex = info.varArgsIndex;
		}
		
		public int getArity() {
			return arity;
		}

		public int getVaargIndex() {
			return varArgsIndex;
		}
	}
	
	
	/**
	 * Full name of a method. For example: LinkedList#append.
	 */
	protected final String fullName;
	
	/**
	 * The name of this callable. For example: append.
	 */
	protected final String funcName;
	
	protected final ParametersInfo parameter;
	
	private boolean isInitializedAttrs;
	
	public CallableObj(String name, ParametersInfo parameter) {
		this(name, name, parameter);
	}
	
	public CallableObj(String funcName,
	                   String fullName, ParametersInfo parameter) {
		this.funcName = funcName;
		this.fullName = fullName;
		this.parameter = parameter;
	}

	@Override
	protected CandyClass initSelfCandyClass() {
		return getCallableClass();
	}
	
	public String funcName() {
		return funcName;
	}
	
	public String fullName() {
		return fullName;
	}
	
	public ParametersInfo getParameter() {
		return parameter;
	}
	
	public int vaargIndex() {
		return getParameter().getVaargIndex();
	}
	
	public int arity() {
		return getParameter().getArity();
	}
	
	/**
	 * Equals call(env, EMPTY_UNPACK_FLAGS, args);
	 */
	public final CandyObject call(CNIEnv env, CandyObject... args) {
		return call(env, EMPTY_UNPACK_FLAGS, args);
	}
	
	/**
	 * Call this function and returns the result.
	 * This is an auxiliary method which help you to call this
	 * function.
	 *
	 * Actually this method will call Evaluator#eval method.
	 */
	public final CandyObject call(CNIEnv env, int unpackFlags, 
	                              CandyObject... args) {
		return env.getEvaluator().eval(this, unpackFlags, args);
	}

	@Override
	public CandyObject getAttr(CNIEnv env, String name) {
		if (!isInitializedAttrs) {
			initAttrs();
			isInitializedAttrs = true;
		}
		return super.getAttr(env, name);
	}

	@Override
	public CandyObject setAttr(CNIEnv env, String name, CandyObject value) {
		if (!isInitializedAttrs) {
			initAttrs();
			isInitializedAttrs = true;
		}
		return super.setAttr(env, name, value);
	}
	
	protected void initAttrs() {
		setBuiltinMetaData("name", StringObj.valueOf(funcName));
		setBuiltinMetaData("fullName", StringObj.valueOf(fullName));
		setBuiltinMetaData("arity", IntegerObj.valueOf(arity()));
		setBuiltinMetaData("vararg", IntegerObj.valueOf(vaargIndex()));
	}
	
	@Override
	public final boolean isCallable() {
		return true;
	}
	
	@Override
	public String toString() {
		return ObjectHelper.toString(
			strTag(), "%s(%d)", fullName, arity());
	}
		
	/**
	 * @see {@link #toString()}
	 */
	protected abstract String strTag();

	public abstract void onCall(CNIEnv env,
	                            OperandStack opStack,
								int argc, int unpackFlags);
	
	public abstract boolean isBuiltin();
}
