package com.nano.candy.interpreter.i2.builtin.type.classes;
import com.nano.candy.interpreter.i2.builtin.CandyObject;

public class ObjectClass extends CandyClass {
	
	public static ObjectClass objClass;
	public static final ObjectClass getObjClass() {
		if (objClass == null) {
			objClass = new ObjectClass();
		}
		return objClass;
	}
	
	public ObjectClass() {
		super("Object", null);
		BuiltinMethodEntity[] methods = BuiltinMethodEntity.
			createMethodEntities(this, CandyObject.class);
		for (BuiltinMethodEntity method : methods) {
			if ("".equals(method.declredName())) {
				setInitalizer(method);
			} else {
				defineMethod(method.declredName(), method);
			}
		}
	}

	@Override
	public CandyClass getSuperClass() {
		return null;
	}
}
