package com.nano.candy.interpreter.i2.builtin.type.classes;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;

public class BuiltinClassFactory {
	
	public static CandyClass generate(Class<? extends CandyObject> clazz) {
		return generate(clazz, null);
	}
	
	public static CandyClass generate(Class<? extends CandyObject> clazz, CandyClass superClass) {
		checkValidClass(clazz);
		BuiltinClass bc = getAnnotation(clazz);
		ClassSignature signature = genClassSignture(clazz, bc, superClass);
		defineMethods(clazz, signature);
		return signature.build();
	}

	private static void checkValidClass(Class<? extends CandyObject> clazz) {
		if (!clazz.isAnnotationPresent(BuiltinClass.class)) {
			throw new Error(clazz.getName() + " is not a built-in class.");
		}
	}
	
	private static BuiltinClass getAnnotation(Class<? extends CandyObject> clazz) {
		return clazz.getDeclaredAnnotation(BuiltinClass.class);
	}
	
	private static ClassSignature genClassSignture(Class<? extends CandyObject> clazz, 
	                                               BuiltinClass anno, 
	                                               CandyClass superClass) {
		if (superClass == null) {
			superClass = ObjectClass.getObjClass();
		}
		return new ClassSignature(anno.value(), superClass)
			.setIsInheritable(anno.isInheritable())
			.setObjEntityClass(clazz);
	}
	
	private static void defineMethods(Class<? extends CandyObject> clazz, ClassSignature sinature) {
		BuiltinMethodEntity[] methods = BuiltinMethodEntity
			.createMethodEntities(sinature.getClassName(), clazz);
		for (BuiltinMethodEntity method : methods) {
			if ("".equals(method.declredName())) {
				sinature.setInitializer(method);
			} else {
				sinature.defineMethod(method);
			}
		}
	}
}
