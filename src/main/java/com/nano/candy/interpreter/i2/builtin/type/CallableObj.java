package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.runtime.OperandStack;
import com.nano.candy.interpreter.i2.runtime.StackFrame;
import com.nano.candy.interpreter.i2.runtime.chunk.ConstantValue;

/**
 * A CallablObj represents a candy callable object (a function, a method
 * or a class).
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
	 * Push arguments into the operand stack in the right-to-left order.
	 */
	public final static void pushArguments(OperandStack opStack,
	                                       CandyObject... args) {
		for (int i = args.length-1; i >= 0; i --) {
			if (args[i] == null) {
				opStack.push(NullPointer.nil());
			} else {
				opStack.push(args[i]);
			}
		}
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

		public int getVarArgsIndex() {
			return varArgsIndex;
		}
	}
	
	
	/**
	 * Full name of a method. For example: LinkedList#append.
	 */
	protected final String fullName;
	protected final String declaredName;
	protected final ParametersInfo parameter;
	
	public CallableObj(String name, ParametersInfo parameter) {
		this(name, name, parameter);
	}
	
	public CallableObj(String declredName,
	                   String fullName, ParametersInfo parameter) {
		this.declaredName = declredName;
		this.fullName = fullName;
		this.parameter = parameter;
	}

	@Override
	protected CandyClass initSelfCandyClass() {
		return getCallableClass();
	}
	
	public String declaredName() {
		return declaredName;
	}
	
	public String fullName() {
		return fullName;
	}
	
	public ParametersInfo getParameter() {
		return parameter;
	}
	
	public int varArgsIndex() {
		return getParameter().getVarArgsIndex();
	}
	
	public int arity() {
		return getParameter().getArity();
	}
	
	public final CandyObject callExeUser(CNIEnv env, CandyObject... args) {
		return callExeUser(env, EMPTY_UNPACK_FLAGS, args);
	}
	
	/**
	 * Execute this callable object.
	 *
	 * @param unpackFlags See {@link ElementsUnpacker#unpackFromStack}
	 */
	public CandyObject callExeUser(CNIEnv env, int unpackFlags, 
	                               CandyObject... args) {
		return env.getEvaluator().eval(this, unpackFlags, args);
	}
	
	public final void call(CNIEnv env, int argc) {
		call(env, argc, EMPTY_UNPACK_FLAGS);
	}
	
	public void call(CNIEnv env, int argc, int unpackFlags) {
		env.getEvaluator().call(this, argc, unpackFlags);
	}

	@Override
	public CandyObject getAttr(CNIEnv env, String name) {
		switch (name) {
			case "name":
				return StringObj.valueOf(declaredName);
			case "fullName":
				return StringObj.valueOf(fullName);
			case "arity":
				return IntegerObj.valueOf(arity());
			case "vararg":
				return IntegerObj.valueOf(varArgsIndex());
		}
		return super.getAttr(env, name);
	}

	@Override
	public CandyObject setAttr(CNIEnv env, String name, CandyObject value) {
		if (isBuiltinAttribute(name)) {
			new AttributeError("The built-in attribute '%s' is read-only.", name)
				.throwSelfNative();
		}
		setMetaData(name, value);
		return value;
	}
	
	protected boolean isBuiltinAttribute(String name) {
		switch (name) {
			case "name":
			case "fullName":
			case "arity":
			case "vararg":
				return true;	
		}
		return false;
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
	                            StackFrame stack, 
								int argc, int unpackFlags);
	
	public abstract boolean isBuiltin();
}
