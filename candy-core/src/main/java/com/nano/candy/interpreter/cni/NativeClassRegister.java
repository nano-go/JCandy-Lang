package com.nano.candy.interpreter.cni;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.ClassSignature;
import com.nano.candy.interpreter.builtin.ObjectClass;
import com.nano.candy.std.Names;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

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
		JavaMethodObj[] methods = NativeMethodRegister
			.generateNativeMethods(sinature.getClassName(), clazz);
		for (JavaMethodObj method : methods) {
			if (Names.METHOD_INITALIZER.equals(method.funcName())) {
				sinature.setInitializer(method);
			} else {
				sinature.defineMethod(method);
			}
		}
	}
	
	private static void verifyClass(Class<? extends CandyObject> clazz) {
		verifyAnnotationPresent(clazz, NativeClass.class);
		NativeClass nativeClass = clazz.getAnnotation(NativeClass.class);
		verifyNativeClass(
			clazz, nativeClass.name(), nativeClass.isInheritable());
	}
	
	protected static void verifyAnnotationPresent(Class c, 
	                                              Class<? extends Annotation> anno) {
		if (!c.isAnnotationPresent(anno)) {
			error("The class %s must be annotated with the %s.",
				  c.getName(), anno.getSimpleName());
		}
	}

	public static void verifyNativeClass(Class<? extends CandyObject> c, 
	                                     String nativeClassName, 
										 boolean isInheritable) {
		if (!Names.isCandyIdentifier(nativeClassName)) {
			error("Invalid name %s of the class %s.", 
				  nativeClassName, c.getName());
		}	
		if (Modifier.isInterface(c.getModifiers())) {
			error("The class %s can't be a interface.", 
				  nativeClassName);
		}
		if (!isInheritable) {
			return;
		}
		try {
			c.getDeclaredConstructor();
		} catch (Exception e) {
			error("The class %s must have a constructor with empty argument"
			      + " if it is an iheritable class.", 
				  nativeClassName);
		}
	}

	protected static void error(String fmt, Object... args) {
		throw new VerifyError(String.format(fmt, args));
	}
}
