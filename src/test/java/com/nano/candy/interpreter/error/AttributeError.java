package com.nano.candy.interpreter.error;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;

public class AttributeError extends CandyRuntimeError{
	
	public static void requiresAttrNonNull(CandyClass type, String attrName, CandyObject attrValue) {
		requiresAttrNonNull(type.getClassName(), attrName, attrValue);
	}
	
	public static void requiresAttrNonNull(String objectName, String attrName, CandyObject attrValue) {
		if (attrValue == null) {
			throw new AttributeError(
				"'%s' object has no attribute '%s'.",
				objectName, attrName
			);
		}
	}
	
	public AttributeError(String msg, Object... args) {
		super(String.format(msg, args)) ;
	}
}
