package com.nano.candy.interpreter.i2.builtin.type.classes;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.nano.candy.interpreter.i2.builtin.CandyObjEntity;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.MethodObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import java.util.HashMap;

/**
 * A candy class provides language level class objects.
 *
 * It's also a callable object. When you call it, an instance will be created
 * and the initalizer of the class will be called.
 */
public class CandyClass extends CallableObj {
	
	/**
	 * A Candy object instance is created by the constructor.
	 */
	protected final ConstructorAccess<? extends CandyObject> constructorAccess;
	
	/**
	 * False if the object entity class (java class) has no a constructor 
	 * with empty parameters or the constructor is private or protected.
	 *
	 * This class cannot create instances if false.
	 */
	protected final boolean canBeCreated;
	
	protected final CandyClass superClass;
	protected final String className;
	protected final boolean isInheritable;
	
	protected final HashMap<String, CallableObj> methods;
	protected final CallableObj initializer;
	
	protected CandyClass(ClassSignature signature) {
		super(null, signature.className, null);	
		this.superClass = signature.superClass;
		this.className = signature.className;
		this.isInheritable = signature.isInheritable;
		this.methods = signature.methods;
		this.initializer = signature.initializer;
		this.constructorAccess = signature.constructorAccess;
		this.canBeCreated = signature.canBeCreated;
		super.parameter = new ParametersInfo(
			arity(), varArgsIndex()
		);
	}
	
	public CallableObj getMethod(String name) {
		CallableObj method = methods.get(name);
		if (method != null) {
			return method;
		}
		if (initializer != null && Names.METHOD_INITALIZER.equals(name)) {
			return initializer;
		}
		return null;
	}
	
	public MethodObj getBoundMethod(String name, CandyObject instance) {
		CallableObj method = getMethod(name);
		if (method != null) {
			return new MethodObj(instance, method);
		}
		return null;
	}
	
	public final boolean isInheritable() {
		return isInheritable;
	}

	public final CallableObj getInitializer() {
		return this.initializer;
	}
	
	public final String getClassName() {
		return className;
	}
	
	public final boolean isSuperClassOf(CandyClass clazz) {
		while (clazz != null) {
			if (clazz == this) {
				return true;
			}
			clazz = clazz.getSuperClass();
		}
		return false;
	}

	public final boolean isSubClassOf(CandyClass clazz) {
		return clazz.isSuperClassOf(this);
	}
	
	public final CandyClass getSuperClass() {
		return superClass;
	}
	
	@Override
	public final CandyClass getCandyClass() {
		return this;
	}
	
	@Override
	public final String getCandyClassName() {
		return getClassName();
	}

	@Override
	public CandyObject getAttr(VM vm, String attr) {
		switch (attr) {
			case "name":
				return StringObj.valueOf(getCandyClassName());
			case "superClass":
				return superClass == null ?
					NullPointer.nil() : superClass;
		}
		return super.getAttr(vm, attr);
	}

	@Override
	public int arity() {
		return initializer == null ? 0 : initializer.arity()-1;
	}

	@Override
	public int varArgsIndex() {
		return initializer == null ? -1 : initializer.varArgsIndex()-1;
	}
	
	@Override
	public boolean isBuiltin() {
		return initializer == null ? true : initializer.isBuiltin();
	}
	
	@Override
	protected void onCall(VM vm, int argc, int unpackFlags) {
		throw new Error("Supported");
	}

	@Override
	public void call(VM vm, int argc, int unpackFlags) {
		CandyObject instance = createInstance(vm);
		new MethodObj(instance, initializer).call(vm, argc, unpackFlags);
	}
	
	protected CandyObject createInstance(VM vm) {		
		if (!canBeCreated) {
			new NativeError(
				"The built-in class can't be instantiated: " 
				+ getCandyClassName()
			).throwSelfNative();
		}
		
		if (constructorAccess == null) {
			return new CandyObjEntity(this);
		}
		CandyObject obj = constructorAccess.newInstance();
		obj.setCandyClass(this);
		return obj;
	}

	@Override
	protected String strTag() {
		return "class";
	}
}
