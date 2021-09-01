package com.nano.candy.interpreter.cni.processor;

import javax.lang.model.type.TypeMirror;

public class TypeNames {
    
	public static final String CANDY_OBJECT_TYPE = "com.nano.candy.interpreter.builtin.CandyObject";
	public static final String CANDY_OBJECT_ARRAY_TYPE = CANDY_OBJECT_TYPE + "[]";
	public static final String CANDY_CNI_ENV = "com.nano.candy.interpreter.cni.CNIEnv";
	
	public static boolean isCandyObject(TypeMirror type) {
		return CANDY_OBJECT_TYPE.equals(type.toString());
	}
	
	public static boolean isCandyObjectArray(TypeMirror type) {
		return CANDY_OBJECT_ARRAY_TYPE.equals(type.toString());
	}
	
	public static boolean isCNIEnc(TypeMirror type) {
		return CANDY_CNI_ENV.equals(type.toString());
	}
}
