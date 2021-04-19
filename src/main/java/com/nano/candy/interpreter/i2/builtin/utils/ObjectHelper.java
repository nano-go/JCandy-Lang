package com.nano.candy.interpreter.i2.builtin.utils;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.functions.BuiltinFunctionEntity;
import com.nano.candy.interpreter.i2.builtin.functions.Callback;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.BoundBuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.MethodCallback;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.vm.VM;
import java.util.Comparator;

public class ObjectHelper {
	
	private ObjectHelper() {}
	
	public static Comparator<CandyObject> newComparator(final VM vm) {
		return new Comparator<CandyObject>() {
			@Override
			public int compare(CandyObject obj1, CandyObject obj2) {
				if (obj1.equalsApiExeUser(vm, obj2).value()) {
					return 0;
				}
				if (obj1.gtApiExeUser(vm, obj2).value()) {
					return 1;
				}
				return -1;
			}
		};
	}
	
	public static void checkIsValidCallable(CandyObject callable, int actualArity) {
		TypeError.checkIsCallable(callable);
		ArgumentError.checkArity(callable, actualArity);
	}
	
	public static void checkNullObject(CandyObject obj, String msg, Object... args) {
		if (obj == null) {
			new AttributeError(String.format(msg, args)).throwSelfNative();
		}
	}
	
	public static String toString(String tag, String content, Object... args) {
		return String.format("<%s: %s>", tag, String.format(content, args));
	}
	
	public static String methodName(CandyClass clazz, String methodName) {
		return methodName(clazz.getClassName(), methodName);
	}
	
	public static String methodName(String className, String methodName) {
		return className + "." + methodName;
	}
	
	public static CallableObj genFunction(String name, int arity, Callback callback) {
		return new BuiltinFunctionEntity(name, arity, callback);
	}
	
	public static CallableObj genMethod(CandyObject receiver, String name, int arity, MethodCallback callback) {
		return new BoundBuiltinMethod(receiver, name, arity, callback);
	}
	
	public static long asInteger(CandyObject obj) {
		TypeError.checkTypeMatched(IntegerObj.INTEGER_CLASS, obj);
		return ((IntegerObj) obj).intValue();
	}
	
	public static double asDouble(CandyObject obj) {
		TypeError.checkTypeMatched(DoubleObj.DOUBLE_CLASS, obj);
		return ((DoubleObj) obj).doubleValue();
	}
	
	public static String asString(CandyObject obj) {
		TypeError.checkTypeMatched(StringObj.STRING_CLASS, obj);
		return ((StringObj) obj).value();
	}
	
	public static CandyObject callFunctionWithArgs(VM vm, CallableObj callable, CandyObject... args) {
		for (int i = args.length-1; i >= 0; i --) {
			vm.push(args[i]);
		}
		callable.onCall(vm);
		if (!callable.isBuiltin()) {
			vm.runFrame(true);
		}
		return vm.pop();
	}
	
	public static CandyObject callFunction(VM vm, CallableObj callable) {
		callable.onCall(vm);
		if (!callable.isBuiltin()) {
			vm.runFrame(true);
		}
		return vm.pop();
	}
}
