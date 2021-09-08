package com.nano.candy.interpreter.builtin.type.error;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.BoolObj;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.DoubleObj;
import com.nano.candy.interpreter.builtin.type.IntegerObj;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.builtin.type.TupleObj;
import com.nano.candy.interpreter.builtin.type.error.ErrorObj;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;

@NativeClass(name = "TypeError", isInheritable = true)
public class TypeError extends ErrorObj {
	public static final CandyClass TYPE_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(TypeError.class, ERROR_CLASS);
	
	public static CandyClass requiresClass(CandyObject obj) {
		if (obj instanceof CandyClass) {
			return (CandyClass) obj;
		}
		new TypeError("The '%s' object is not a Class.", 
			obj.toString()).throwSelfNative();
		return null;
	}
	
	private static void throwTypeError(String expectedClassName, String actualClassName) {
		new TypeError(
			"The '%s' can't apply to '%s' obj.",
			actualClassName, expectedClassName
		).throwSelfNative();	
	}
	
	public static CallableObj requiresCallable(CandyObject obj) {
		if (obj instanceof CallableObj) {
			return (CallableObj) obj;
		}
		throwTypeError(CallableObj.getCallableClass().getName(), obj.getCandyClassName());
		return null;
	}
	
	public static DoubleObj requiresDoubleObj(CandyObject obj) {
		if (obj instanceof DoubleObj) {
			return (DoubleObj) obj;
		}
		throwTypeError(DoubleObj.DOUBLE_CLASS.getName(), obj.getCandyClassName());
		return null;
	}
	
	public static IntegerObj requiresIntegerObj(CandyObject obj) {
		if (obj instanceof IntegerObj) {
			return (IntegerObj) obj;
		}
		throwTypeError(IntegerObj.INTEGER_CLASS.getName(), obj.getCandyClassName());
		return null;
	}
	
	public static StringObj requiresStringObj(CandyObject obj) {
		if (obj instanceof StringObj) {
			return (StringObj) obj;
		}
		throwTypeError(StringObj.STRING_CLASS.getName(), obj.getCandyClassName());
		return null;
	}
	
	public static BoolObj requiresBoolObj(CandyObject obj) {
		if (obj instanceof BoolObj) {
			return (BoolObj) obj;
		}
		throwTypeError(BoolObj.BOOL_CLASS.getName(), obj.getCandyClassName());
		return null;
	}
	
	public static TupleObj requiresTupleObj(CandyObject obj) {
		if (obj instanceof TupleObj) {
			return (TupleObj) obj;
		}
		throwTypeError(TupleObj.TUPLE_CLASS.getName(), obj.getCandyClassName());
		return null;
	}
	public static ArrayObj requiresArrayObj(CandyObject obj) {
		if (obj instanceof ArrayObj) {
			return (ArrayObj) obj;
		}
		throwTypeError(ArrayObj.ARRAY_CLASS.getName(), obj.getCandyClassName());
		return null;
	}
	
	public static CandyObject[] requirsCandyObjectArray(CandyObject obj) {
		if (obj instanceof ArrayObj) {
			return ((ArrayObj) obj).subarray(0, ((ArrayObj) obj).length());
		}
		throwTypeError(ArrayObj.ARRAY_CLASS.getName(), obj.getCandyClassName());
		return null;
	}
	
	public static void checkIsCallable(CandyObject callable) {
		if (!callable.isCallable()) {
			new TypeError("The '%s' object is not callable.", 
				callable.getCandyClassName()
			).throwSelfNative();
		}
	}
	
	public static void checkClassMatched(CandyClass expected, CandyClass clazz) {
		if (clazz != expected) {
			new TypeError("The '%s' class can't apply to '%s' class.",
				clazz.getCandyClassName(), expected.getName()
			).throwSelfNative();
		}
	}

	public static void checkTypeMatched(CandyClass expected, CandyObject instance) {
		if (!expected.isSuperClassOf(instance.getCandyClass())) {
			new TypeError(
				"The '%s' can't apply to '%s' obj.",
				instance.getCandyClassName(), expected.getName()
			).throwSelfNative();	
		}
	}
	
	public static void checkIsInstanceOf(CandyClass expected, CandyObject instance) {
		if (!instance.isInstanceOf(expected)) {
			new TypeError(
				"The '%s' can't apply to '%s' obj.",
				instance.getCandyClassName(), expected.getName()
			).throwSelfNative();	
		}
	}

	public static void checkTypeMatched(CandyClass expected, CandyObject instance, String msg, Object... args) {
		if (instance.isCandyClass() || !expected.isSuperClassOf(instance.getCandyClass())) {
			new TypeError(msg, args).throwSelfNative();
		}
	}
	
	public TypeError() {
		super(TYPE_ERROR_CLASS);
	}
	
	public TypeError(String msgFmt, Object... args) {
		super(TYPE_ERROR_CLASS, msgFmt, args);
	}
	
	public TypeError(String msg) {
		super(TYPE_ERROR_CLASS, msg);
	}
}
