package com.nano.candy.interpreter.builtin;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.cni.JavaMethodObj;
import com.nano.candy.interpreter.cni.NativeMethodRegister;
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
		JavaMethodObj[] methods = NativeMethodRegister.
			generateNativeMethods("Object", CandyObject.class);
		ClassSignature signature = new ClassSignature("Object", null);
		for (JavaMethodObj method : methods) {
			if (Names.METHOD_INITALIZER.equals(method.funcName())) {
				signature.setInitializer(method);
			} else {
				signature.defineMethod(method);
			}
		}
		return signature;
	}
	
	private ObjectClass() {}
	
}
