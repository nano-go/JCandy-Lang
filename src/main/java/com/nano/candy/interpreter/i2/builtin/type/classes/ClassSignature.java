package com.nano.candy.interpreter.i2.builtin.type.classes;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import java.util.HashMap;

/**
 * Builds a candy class with this signature.
 */
public class ClassSignature {
	protected Class<? extends CandyObject> objEntityClass;

	protected CandyClass superClass;
	protected String className;
	protected boolean isInheritable;

	protected HashMap<String, CallableObj> methods;
	protected CallableObj initializer;
	
	/**
	 * If the super class is non-null, this class will inherit all the methods and
	 * the initializer of the super class.
	 */
	public ClassSignature(String className, CandyClass superClass) {
		this.className = className;
		this.superClass = superClass;
		
		if (this.superClass == null) {
			this.methods = new HashMap<>();
		} else {
			this.methods = new HashMap<>(superClass.methods);
			this.initializer = superClass.initializer;
			this.objEntityClass = superClass.objEntityClass;
		}
	}

	public CandyClass getSuperClass() {
		return superClass;
	}

	public String getClassName() {
		return className;
	}

	public boolean isInheritable() {
		return isInheritable;
	}

	public ClassSignature setObjEntityClass(Class<? extends CandyObject> objEntityClass) {
		this.objEntityClass = objEntityClass;
		return this;
	}

	public ClassSignature setIsInheritable(boolean isInheritable) {
		this.isInheritable = isInheritable;
		return this;
	}
	
	public ClassSignature defineMethod(CallableObj obj) {
		return defineMethod(obj.declaredName(), obj);
	}

	public ClassSignature defineMethod(String name, CallableObj obj) {
		methods.put(name, obj);
		return this;
	}

	public ClassSignature setInitializer(CallableObj initializer) {
		this.initializer = initializer;
		return this;
	}
	
	public CandyClass build() {
		return new CandyClass(this);
	} 
}
