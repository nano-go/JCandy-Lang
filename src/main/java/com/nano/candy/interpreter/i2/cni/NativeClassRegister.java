package com.nano.candy.interpreter.i2.cni;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.ClassSignature;
import com.nano.candy.interpreter.i2.builtin.ObjectClass;
import com.nano.candy.std.Names;

public class NativeClassRegister {
	
	public static CandyClass generateNativeClass(Class<? extends CandyObject> clazz) {
		return generateNativeClass(clazz, null);
	}

	public static CandyClass generateNativeClass(Class<? extends CandyObject> clazz, 
	                                             CandyClass superClass) {
		verifyClass(clazz);
		NativeClass anno = clazz.getDeclaredAnnotation(NativeClass.class);
		ClassSignature signature = getClassSignture(clazz, anno, superClass);
		defineMethods(clazz, signature);
		return signature.build();
	}

	private static ClassSignature getClassSignture(Class<? extends CandyObject> clazz, 
	                                               NativeClass anno, 
	                                               CandyClass superClass) {
		if (superClass == null) {
			superClass = ObjectClass.getObjClass();
		}
		return new ClassSignature(anno.name(), superClass)
			.setIsInheritable(anno.isInheritable())
			.setObjEntityClass(clazz);
	}

	private static void defineMethods(Class<? extends CandyObject> clazz, 
	                                  ClassSignature sinature) {
		CNativeMethod[] methods = NativeMethodRegister
			.generateNativeMethods(sinature.getClassName(), clazz);
		for (CNativeMethod method : methods) {
			if (Names.METHOD_INITALIZER.equals(method.funcName())) {
				sinature.setInitializer(method);
			} else {
				sinature.defineMethod(method);
			}
		}
	}
	
	
	private static void verifyClass(Class<? extends CandyObject> clazz) {
		AnnotationSigntureVerifier
			.verifyAnnotationPresent(clazz, NativeClass.class);
		NativeClass nativeClass = clazz.getAnnotation(NativeClass.class);
		AnnotationSigntureVerifier.verifyNativeClass(
			clazz, nativeClass.name(), nativeClass.isInheritable());
	}
}
