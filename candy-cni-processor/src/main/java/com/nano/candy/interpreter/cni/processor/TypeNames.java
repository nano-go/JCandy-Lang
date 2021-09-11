package com.nano.candy.interpreter.cni.processor;

import javax.lang.model.type.TypeMirror;

public class TypeNames {
	
	public static final String CANDY_CNI_ENV = "com.nano.candy.interpreter.cni.CNIEnv";
    
	public static final String CANDY_OBJECT_TYPE = "com.nano.candy.interpreter.builtin.CandyObject";
	public static final String CANDY_OBJECT_ARRAY_TYPE = CANDY_OBJECT_TYPE + "[]";
	public static final String CANDY_STRING_TYPE = className("StringObj");
	public static final String CANDY_INTEGER_TYPE = className("IntegerObj");
	public static final String CANDY_DOUBLE_TYPE = className("DoubleObj");
	public static final String CANDY_BOOL_TYPE = className("BoolObj");
	public static final String CANDY_ARRAY_TYPE = className("ArrayObj");
	public static final String CANDY_TUPLE_TYPE = className("TupleObj");
	public static final String CANDY_CALLABLE_TYPE = className("CallableObj");
	
	public static final String OPTIONAL_ARG_TYPE = "com.nano.candy.interpreter.builtin.utils.OptionalArg";
	
	public static final String[] BASE_CANDY_OBJ_TYPES = {
		CANDY_OBJECT_TYPE, CANDY_STRING_TYPE, CANDY_INTEGER_TYPE, CANDY_DOUBLE_TYPE, CANDY_BOOL_TYPE,
		CANDY_ARRAY_TYPE, CANDY_TUPLE_TYPE, CANDY_CALLABLE_TYPE
	};
	
	public static final Class<?>[] SUPPORTED_PRI_TYPES = {
		byte.class, short.class, int.class, long.class,
		float.class, double.class, boolean.class, String.class
	};
	
	private static final String className(String className) {
		return "com.nano.candy.interpreter.builtin.type." + className;
	}
	
	public static boolean isBaseCandyObjType(TypeMirror type) {
		for (String baseType : BASE_CANDY_OBJ_TYPES) {
			if (baseType.equals(type.toString())) return true;
		}
		return false;
	}
	
	public static boolean isSupportedPriType(TypeMirror type) {
		for (Class<?> t : SUPPORTED_PRI_TYPES) {
			if (t.getName().equals(type.toString())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isOptionalArg(TypeMirror type) {
		return OPTIONAL_ARG_TYPE.equals(type.toString());
	}
	
	public static boolean isCandyObject(TypeMirror type) {
		return CANDY_OBJECT_TYPE.equals(type.toString());
	}
	
	public static boolean isArrayObj(TypeMirror type) {
		return CANDY_ARRAY_TYPE.equals(type.toString());
	}
	
	public static boolean isCandyObjectArray(TypeMirror type) {
		return CANDY_OBJECT_ARRAY_TYPE.equals(type.toString());
	}
	
	public static boolean isCNIEnc(TypeMirror type) {
		return CANDY_CNI_ENV.equals(type.toString());
	}
}
