package com.nano.candy.interpreter.builtin.utils;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.IntegerObj;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.interpreter.builtin.type.NumberObj;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.cni.CNIEnv;
import java.util.ArrayList;
import java.util.Comparator;

public class ObjectHelper {
	
	private ObjectHelper() {}
	
	public static Comparator<CandyObject> newComparator(final CNIEnv env) {
		return new Comparator<CandyObject>() {
			@Override
			public int compare(CandyObject obj1, CandyObject obj2) {
				if (obj1.callEquals(env, obj2).value()) {
					return 0;
				}
				if (obj1.callGt(env, obj2).value()) {
					return 1;
				}
				return -1;
			}
		};
	}
	
	public static Comparator<CandyObject> newComparator(final CNIEnv env, CandyObject cmp) {
		final CallableObj cmpFn = TypeError.requiresCallable(cmp);
		return new Comparator<CandyObject>() {
			@Override
			public int compare(CandyObject obj1, CandyObject obj2) {
				CandyObject obj1Attr = callFunction(env, cmpFn, obj1);
				CandyObject obj2Attr = callFunction(env, cmpFn, obj2);
				if (obj1Attr.callEquals(env, obj2Attr).value()) {
					return 0;
				}
				if (obj1Attr.callGt(env, obj2Attr).value()) {
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
		return methodName(clazz.getName(), methodName);
	}
	
	public static String methodName(String className, String methodName) {
		return className + "." + methodName;
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
	
	public static CandyObject[] iterableObjToArray(CNIEnv env, CandyObject iterableObj) {
		CandyObjectIterator iterator = new CandyObjectIterator(env, iterableObj);
		ArrayList<CandyObject> elements = new ArrayList<>();
		while (iterator.hasNext()) {
			elements.add(iterator.next());
		}
		return elements.toArray(new CandyObject[elements.size()]);
	}
	
	public static CandyObject callFunction(CNIEnv env, CallableObj callable, CandyObject... args) {
		return callable.call(env, args);
	}
}
