package com.nano.candy.interpreter.i2.builtin.type.classes;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;

public class BuiltinClassFactory {
	
	public static CandyClass generate(Class<? extends CandyObject> clazz) {
		return generate(clazz, null);
	}
	
	public static CandyClass generate(Class<? extends CandyObject> clazz, CandyClass superClass) {
		checkValidClass(clazz);
		BuiltinClass bc = getBuiltinClassAnno(clazz);
		CandyClass candyClass = generateCandyClass(clazz, bc, superClass);
		defineMethods(clazz, candyClass);
		return candyClass;
	}

	private static void checkValidClass(Class<? extends CandyObject> clazz) {
		if (!clazz.isAnnotationPresent(BuiltinClass.class)) {
			throw new Error(clazz.getName() + " is not a built-in class.");
		}
	}
	
	private static BuiltinClass getBuiltinClassAnno(Class<? extends CandyObject> clazz) {
		return clazz.getDeclaredAnnotation(BuiltinClass.class);
	}
	
	private static CandyClass generateCandyClass(Class<? extends CandyObject> clazz, BuiltinClass builtinClass, 
	                                             CandyClass superClass) {
		String className = builtinClass.value();
		if (superClass == null) {
			superClass = ObjectClass.getObjClass();
		}
		CandyClass candyClass = new CandyClass(className, superClass, builtinClass.isInheritable());
		candyClass.setObjectEntityClass(clazz);
		return candyClass;
	}
	
	private static void defineMethods(Class<? extends CandyObject> clazz, CandyClass candyClass) {
		BuiltinMethodEntity[] methods = BuiltinMethodEntity.createMethodEntities(
			candyClass, clazz);
		for (BuiltinMethodEntity method : methods) {
			if ("".equals(method.declredName())) {
				candyClass.setInitalizer(method);
			} else {
				candyClass.defineMethod(method.declredName(), method);
			}
		}
	}
}
