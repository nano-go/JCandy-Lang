package com.nano.candy.interpreter.i2.builtin.type.classes;
import com.nano.candy.interpreter.i2.builtin.CandyObject;

public class ObjectClass {
	
	public static CandyClass objClass;
	public static final CandyClass getObjClass() {
		if (objClass == null) {
			objClass = new CandyClass(getObjectClassSignature());
		}
		return objClass;
	}
	
	private static ClassSignature getObjectClassSignature() {
		BuiltinMethodEntity[] methods = BuiltinMethodEntity.
			createMethodEntities("Object", CandyObject.class);
		ClassSignature signature = new ClassSignature("Object", null);
		for (BuiltinMethodEntity method : methods) {
			if ("".equals(method.declredName())) {
				signature.setInitializer(method);
			} else {
				signature.defineMethod(method);
			}
		}
		return signature;
	}
	
	private ObjectClass() {}
	
}
