package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.code.ConstantValue;
import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.runtime.OperandStack;

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
	
	/**
	 * Full name of a method. For example: LinkedList#append.
	 */
	protected final String fullName;
	
	/**
	 * The name of this callable. For example: append.
	 */
	protected final String funcName;
	
	/**
	 * The number of arguments that this function can take.
	 */
	private final int arity;

	/**
	 * It means {@code parameters[varargsIndex]} can accept variable-length
	 * arguments.
	 *
	 * If this value is -1, it means no such parameters.
	 */
	private final int varargsIndex;
	
	private final int optionalArgFlags;
	
	private boolean isInitializedAttrs;
	
	public CallableObj(String funcName, String fullName, 
	                   ConstantValue.MethodInfo met) {
		this(funcName, fullName, met.arity, met.varArgsIndex, met.optionalArgFlags);
	}
	
	public CallableObj(String name, 
	                   int arity,
	                   int varargsIndex,
	                   int optionalArgFlags) {
		this(name, name, arity, varargsIndex, optionalArgFlags);
	}
	
	public CallableObj(String funcName,
	                   String fullName, 
	                   int arity, int varargsIndex, int optionalArgFlags) {
		this.funcName = funcName;
		this.fullName = fullName;
		this.arity = arity;
		this.varargsIndex = varargsIndex;
		this.optionalArgFlags = optionalArgFlags;
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
	
	public int vaargIndex() {
		return varargsIndex;
	}
	
	public int optionalArgFlags() {
		return optionalArgFlags;
	}
	
	public int optionalArgCount() {
		return Integer.bitCount(optionalArgFlags);
	}
	
	public int arity() {
		return arity;
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
		setBuiltinMetaData("varargs", IntegerObj.valueOf(vaargIndex()));
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
