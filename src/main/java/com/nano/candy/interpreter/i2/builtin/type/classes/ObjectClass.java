package com.nano.candy.interpreter.i2.builtin.type.classes;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.cni.CNativeMethod;
import com.nano.candy.interpreter.i2.cni.NativeMethodRegister;
import com.nano.candy.std.Names;

public class ObjectClass {
	
	public static CandyClass objClass;
	public static final CandyClass getObjClass() {
		if (objClass == null) {
			objClass = new CandyClass(getObjectClassSignature());
		}
		return objClass;
	}
	
	private static ClassSignature getObjectClassSignature() {
		CNativeMethod[] methods = NativeMethodRegister.
			generateNativeMethods("Object", CandyObject.class);
		ClassSignature signature = new ClassSignature("Object", null);
		for (CNativeMethod method : methods) {
			if (Names.METHOD_INITALIZER.equals(method.declaredName())) {
				signature.setInitializer(method);
			} else {
				signature.defineMethod(method);
			}
		}
		return signature;
	}
	
	private ObjectClass() {}
	
}
