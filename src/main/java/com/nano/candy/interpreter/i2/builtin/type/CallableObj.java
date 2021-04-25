package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.i2.builtin.utils.ElementsUnpacker;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.rtda.OperandStack;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.vm.VM;

/**
 * A callable represents a candy callable object (a function, a method
 * or a class).
 */
@NativeClass(name = "Callable")
public abstract class CallableObj extends BuiltinObject {
	
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
	 * Push arguments into the operand stack in right-to-left order.
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
		 * The number of arguments that a callable object can take.
		 */
		private int arity;

		/**
		 * The parameter that the index is entry can accept zero or 
		 * more arguments.
		 *
		 * If this value is -1, it means no such parameters.
		 */
		private int varArgsIndex;

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
	 * The name is declared in programe.
	 */
	protected String declaredName;
	
	/**
	 * The name is full name of a method, for example: LinkedList#append.
	 */
	protected String fullName;
	
	protected ParametersInfo parameter;
	
	public CallableObj(String name, ParametersInfo parameter) {
		this(name, name, parameter);
	}
	
	public CallableObj(String declredName,
	                   String name, ParametersInfo parameter) {
		super(null);
		this.declaredName = declredName;
		this.fullName = name;
		this.parameter = parameter;
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
	
	@NativeMethod(name = "arity")
	private final CandyObject arity(VM vm, CandyObject[] args) {
		return IntegerObj.valueOf(arity());
	}
	
	@NativeMethod(name = "name")
	private final CandyObject name(VM vm, CandyObject[] args) {
		return StringObj.valueOf(declaredName());
	}
	
	protected final CandyObject[] unpack(VM vm, int argc, int unpackFlags) {
		CandyObject[] args = ElementsUnpacker.unpackFromStack
			(vm, argc, varArgsIndex(), arity(), unpackFlags);
		if (args == null) {
			ArgumentError.throwsArgumentError(this, argc);
		}
		return args;
	}
	
	public final CandyObject callExeUser(VM vm, CandyObject... args) {
		return callExeUser(vm, EMPTY_UNPACK_FLAGS, args);
	}
	
	/**
	 * Execute this callable object and returns result.
	 *
	 * If this callable object is a prototype function, vm will run
	 * the frame after the prototype function calls.
	 *
	 * @param unpackFlags See {@link ElementsUnpacker#unpackFromStack}
	 */
	public CandyObject callExeUser(VM vm, int unpackFlags, 
	                               CandyObject... args) {
		if (args != null) {
			pushArguments(vm.frame().getOperandStack(), args);
		}
		call(vm, args == null ? 0 : args.length, unpackFlags);
		if (!isBuiltin()) {
			vm.runFrame(true);
		}
		return vm.pop();
	}
	
	public final void call(VM vm, int argc) {
		call(vm, argc, EMPTY_UNPACK_FLAGS);
	}

	/**
	 * Usually called by the VM.
	 */
	public void call(VM vm, int argc, int unpackFlags) {
		if (unpackFlags != EMPTY_UNPACK_FLAGS || varArgsIndex() != -1) {
			CandyObject[] args = unpack(vm, argc, unpackFlags);
			pushArguments(vm.frame().getOperandStack(), args);
			onCall(vm, args.length, unpackFlags);
		} else {
			if (this.arity() != argc) {
				ArgumentError.throwsArgumentError(this, argc);
			}
			onCall(vm, argc, unpackFlags);
		}
	}
	
	@Override
	public CandyClass getCandyClass() {
		return getCallableClass();
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

	protected abstract void onCall(VM vm, int argc, int unpackFlags);
	
	public abstract boolean isBuiltin();
	
	/**
	 * @see {@link #toString()}
	 */
	protected abstract String strTag();
}
