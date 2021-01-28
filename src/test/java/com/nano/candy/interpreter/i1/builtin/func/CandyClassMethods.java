package com.nano.candy.interpreter.i1.builtin.func;

import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.BooleanObject;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;

public class CandyClassMethods {
	
	private CandyObject isSubClassOf;
	private CandyObject isSuperClassOf;

	private CandyClass binder;
	
	public CandyClassMethods(CandyClass binder) {
		this.binder = binder;
	}
	
	public CandyObject isSubClassOf() {
		if (isSubClassOf != null) return isSubClassOf;
		isSubClassOf = BuiltinMethodHelper.generateMethod(binder, 1, new BuiltinMethodHelper.Callback() {
			@Override
			public CandyObject onCall(AstInterpreter i, CandyObject instance, CandyObject[] args) {
				return BooleanObject.valueOf(
					((CandyClass)instance).isSubclassOf(args[0]._class())
				);
			}
		});
		return isSubClassOf;
	}
	
	public CandyObject isSuperClassOf() {
		if (isSuperClassOf != null) return isSuperClassOf;
		isSuperClassOf = BuiltinMethodHelper.generateMethod(binder, 1, new BuiltinMethodHelper.Callback() {
			@Override
			public CandyObject onCall(AstInterpreter i, CandyObject instance, CandyObject[] args) {
				return BooleanObject.valueOf(
					((CandyClass)instance).isSuperclassOf(args[0]._class())
				);
			}
		});
		return isSuperClassOf;
	}
	
}
