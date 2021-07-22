package com.nano.candy.interpreter.i2.builtin;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import java.util.HashMap;
import java.lang.reflect.Modifier;

/**
 * Builds a Candy class with this signature.
 */
public class ClassSignature {
	protected ConstructorAccess<? extends CandyObject> constructorAccess;
	protected boolean canBeCreated;
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
			this.constructorAccess = superClass.constructorAccess;
		}
		this.canBeCreated = true;
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
		try {
			// Abstract class can't create instances!
			if (Modifier.isAbstract(objEntityClass.getClass().getModifiers())) {
				this.canBeCreated = false;
				return this;
			}
			this.constructorAccess = ConstructorAccess.get(objEntityClass);			
		} catch (RuntimeException e) {
			this.constructorAccess = null;
			this.canBeCreated = false;
		}
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
