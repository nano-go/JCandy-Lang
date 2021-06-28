package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;

@NativeClass(name = "AttributeError", isInheritable = true)
public class AttributeError extends ErrorObj {
	public static final CandyClass ATTR_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(AttributeError.class, ERROR_CLASS);

	public static void checkAttributeNull(CandyObject obj, String attrStr, CandyObject attr) {
		if (attr == null) {
			new AttributeError("'%s' object has no attribute '%s'.", 
				obj.getCandyClassName(), attrStr
			).throwSelfNative();
		}
	}
		
	public AttributeError() {
		super(ATTR_ERROR_CLASS);
	}
	
	public AttributeError(String errmsg) {
		super(ATTR_ERROR_CLASS, errmsg);
	}
	
	public AttributeError(String msgFmt, Object... args) {
		super(ATTR_ERROR_CLASS, msgFmt, args);
	}
}
