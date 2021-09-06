package com.nano.candy.interpreter.builtin;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.error.OverrideError;
import com.nano.candy.std.CandyAttrSymbol;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Builds a Candy class with this signature.
 */
public class ClassSignature {
	
	protected Constructor<? extends CandyObject> objectAllocator;
	protected boolean canBeInstantiated;
	protected CandyClass superClass;
	protected String className;
	protected boolean isInheritable;

	protected HashMap<String, CallableObj> methods;
	protected Set<CandyAttrSymbol> attrs;
	protected CallableObj initializer;
	
	public ClassSignature(String className, CandyClass superClass) {
		this.className = className;
		this.superClass = superClass;
		
		if (this.superClass == null) {
			this.methods = new HashMap<>();
			this.attrs = Collections.emptySet();
		} else {
			this.methods = new HashMap<>(superClass.methods);
			this.attrs = new HashSet<>(superClass.predefinedAttrs);
			this.initializer = superClass.initializer;
			this.objectAllocator = superClass.objectAllocator;
		}
		this.canBeInstantiated = true;
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
	
	public ClassSignature setPredefinedAttrs(Set<CandyAttrSymbol> attrs) {
		if (this.attrs.isEmpty()) {
			this.attrs = attrs;
		} else {
			for (CandyAttrSymbol attr : attrs) {
				if (this.attrs.contains(attr)) {
					new OverrideError(
						"The attribute '%s' can not be override.",
						attr.getName()
					).throwSelfNative();
				}
				this.attrs.add(attr);
			}
		}
		return this;
	}
	
	public ClassSignature setObjEntityClass(Class<? extends CandyObject> objEntityClass) {
		try {
			// Abstract classes can't create instances!
			if (Modifier.isAbstract(objEntityClass.getClass().getModifiers())) {
				this.canBeInstantiated = false;
				return this;
			}
			this.objectAllocator = objEntityClass.getDeclaredConstructor();
			if (Modifier.isPublic(objectAllocator.getModifiers())) {
				return this;
			}
			if (Modifier.isProtected(objectAllocator.getModifiers())) {
				objectAllocator.setAccessible(true);
				return this;
			}
		} catch (Exception e) {
			// It means that can't get the allocator.
		}
		this.objectAllocator = null;
		this.canBeInstantiated = false;
		return this;
	}

	public ClassSignature setIsInheritable(boolean isInheritable) {
		this.isInheritable = isInheritable;
		return this;
	}
	
	public ClassSignature defineMethod(CallableObj obj) {
		return defineMethod(obj.funcName(), obj);
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
