package com.nano.candy.interpreter.i2.rtda;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.functions.BuiltinFunctionEntity;
import com.nano.candy.interpreter.i2.builtin.functions.BuiltinFunctions;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.NumberObj;
import com.nano.candy.interpreter.i2.builtin.type.Range;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.ObjectClass;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class GlobalEnvironment {
	
	private HashMap<String, CandyObject> vars;
	
	public GlobalEnvironment() {
		vars = new HashMap<>();
		init();
	}
	
	private void init() {
		defineBuiltinFunctions();
		defineClass(Range.RANGE_CLASS);
		defineClass(ArrayObj.ARRAY_CLASS);
		defineClass(IntegerObj.INTEGER_CLASS);
		defineClass(NumberObj.NUMBER_CLASS);
		defineClass(DoubleObj.DOUBLE_CLASS);
		defineClass(StringObj.STRING_CLASS);
		defineClass(BoolObj.BOOL_CLASS);
		defineClass(ObjectClass.getObjClass());
	}

	private void defineBuiltinFunctions() {
		for (Field field : BuiltinFunctions.class.getFields()) {
			int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers)) {
				continue;
			}
			if (field.getType() == BuiltinFunctionEntity.class) {
				try {
					defineBuiltinFunction(
						(BuiltinFunctionEntity) field.get(null));
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		}
	}

	private void defineBuiltinFunction(BuiltinFunctionEntity func) {
		vars.put(func.declredName(), func);
	}

	private void defineClass(CandyClass clazz) {
		vars.put(clazz.getClassName(), clazz);
	}
	
	public CandyObject getVar(String name) {
		return vars.get(name);
	}
	
	public void setVar(String name, CandyObject value) {
		vars.put(name, value);
	}
	
}
