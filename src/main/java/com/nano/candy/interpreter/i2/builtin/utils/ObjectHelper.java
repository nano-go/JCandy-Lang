package com.nano.candy.interpreter.i2.builtin.utils;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.NumberObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
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
	
	public static void checkNullObject(CandyObject obj, String msg, Object... args) {
		if (obj == null) {
			new AttributeError(String.format(msg, args)).throwSelfNative();
		}
	}
	
	public static CandyObject preventNull(CandyObject obj) {
		return obj == null ? NullPointer.nil() : obj;
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
	
	public static String callStr(VM vm, CandyObject obj) {
		return obj.strApiExeUser(vm).value();
	}
	
	public static CandyObject setAttr(VM vm, CandyObject obj, String attr, CandyObject value) {
		return obj.setAttrApiExeUser(vm, attr, value);
	}
	
	public static CandyObject getAttr(VM vm, CandyObject obj, String attr) {
		return obj.getAttrApiExeUser(vm, attr);
	}
	
	public static CandyObject setItem(VM vm, CandyObject obj, CandyObject key, CandyObject value) {
		return obj.setItemApiExeUser(vm, key, value);
	}

	public static CandyObject getItem(VM vm, CandyObject obj, CandyObject key) {
		return obj.getItemApiExeUser(vm, key);
	}
	
	public static long asInteger(CandyObject obj) {
		TypeError.checkTypeMatched(IntegerObj.INTEGER_CLASS, obj);
		return ((IntegerObj) obj).intValue();
	}
	
	public static double asDouble(CandyObject obj) {
		TypeError.checkTypeMatched(NumberObj.NUMBER_CLASS, obj);
		return ((NumberObj) obj).doubleValue();
	}
	
	public static String asString(CandyObject obj) {
		TypeError.checkTypeMatched(StringObj.STRING_CLASS, obj);
		return ((StringObj) obj).value();
	}
	
	public static CandyObject callFunction(VM vm, CallableObj callable, CandyObject... args) {
		return callable.callExeUser(vm, args);
	}
}
