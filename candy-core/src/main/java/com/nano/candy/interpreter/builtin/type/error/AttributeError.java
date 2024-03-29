package com.nano.candy.interpreter.builtin.type.error;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;

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
	
	public static void throwHasNoAttr(CandyObject object, String name) {
		new AttributeError(
			"'%s' object has no attribute '%s'.", 
			object.getCandyClassName(), name
		).throwSelfNative();
	}
	
	public static void throwReadOnlyError(String name) {
		new AttributeError("The attribute '%s' is read-only.", name)
			.throwSelfNative();
	}
	
	public static void throwWriteOnlyError(String name) {
		new AttributeError("The attribute '%s' is write-only.", name)
			.throwSelfNative();
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
