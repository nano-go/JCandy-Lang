package com.nano.candy.interpreter.i2.error;
import com.nano.candy.interpreter.i2.builtin.CandyObject;

public class AttributeError extends CandyRuntimeError {
	
	public static void checkAttributeNull(CandyObject obj, String attrStr, CandyObject attr) {
		if (attr == null) {
			throw new AttributeError(
				"'%s' object has no attribute '%s'.", 
				obj.getCandyClassName(), attrStr
			);
		}
	}
	
	public AttributeError(String format, Object... args) {
		super(format, args);
	}
}
