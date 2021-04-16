package com.nano.candy.interpreter.i2.builtin.type.classes;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObjEntity;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.MethodObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.vm.CarrierErrorException;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * A candy class provides language level class object, it's a built-in object.
 */
public class CandyClass extends BuiltinObject {
	
	/**
	 * An instance is created by reflection of the entity class when
	 * this class is called.
	 */
	private Class<? extends CandyObject> objEntityClass;
	
	private CandyClass superClass;
	private String className;
	private boolean isInheritable;
	
	private HashMap<String, CallableObj> methods;
	private CallableObj initializer;
	
	public CandyClass(String name) {
		this(name, ObjectClass.getObjClass(), true);
	}
	
	public CandyClass(String name, CandyClass superClass) {
		this(name, superClass, true);
	}
	
	/**
	 * If the super class is non-null, this class will inherit all the methods and
	 * the initializer of the super class.
	 */
	public CandyClass(String name, CandyClass superClass, boolean isInheritable) {
		super(null);
		this.className = name;
		this.superClass = superClass;
		this.isInheritable = isInheritable;
		if (superClass != null) {
			this.methods = new HashMap<String, CallableObj>(superClass.methods);
			this.initializer = superClass.initializer;
			this.objEntityClass = superClass.getObjEntityClass();
		} else {
			this.methods = new HashMap<>();
		}
	}
	
	protected void setObjectEntityClass(Class<? extends CandyObject> objEntityClass) {
		this.objEntityClass = objEntityClass;
	}
	
	public Class<? extends CandyObject> getObjEntityClass() {
		return objEntityClass;
	}
	
	public boolean isBuiltinClass() {
		return objEntityClass != null && objEntityClass != CandyObjEntity.class;
	}
	
	public boolean isInheritable() {
		return isInheritable;
	}
	
	public void setInitalizer(CallableObj initalizer) {
		this.initializer = initalizer;
	}
	
	public CallableObj getInitializer() {
		return this.initializer;
	}
	
	/**
	 * Returns the unbound method by the given name or null if
	 * not found.
	 */
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
	
	/**
	 * Returns the method is bound with the given instance or null
	 * if not found.
	 */
	public MethodObj getBoundMethod(String name, CandyObject instance) {
		CallableObj method = getMethod(name);
		if (method != null) {
			return new MethodObj(instance, method);
		}
		return null;
	}
	
	public final void defineMethod(String name, CallableObj obj) {
		methods.put(name, obj);
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
	
	public CandyClass getSuperClass() {
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
				return superClass;
		}
		return super.getAttr(vm, attr);
	}

	@Override
	public int arity() {
		return initializer == null ? 0 : initializer.arity()-1;
	}

	@Override
	public boolean isCallable() {
		return true;
	}

	@Override
	public void onCall(VM vm) {
		CandyObject instance = createInstance(vm);
		if (initializer == null) {
			vm.returnFromVM(instance);
		} else {
			vm.push(instance);
			initializer.onCall(vm);
		}
	}
	
	protected CandyObject createInstance(VM vm) {
		if (objEntityClass == null) {
			return new CandyObjEntity(this);
		}
		Constructor<? extends CandyObject> constructor;
		try {
			constructor = objEntityClass.getConstructor();
		} catch (NoSuchMethodException | SecurityException  e) {
			new NativeError(
				"The built-in class can't be instantiated: " + getCandyClassName()
			).throwSelfNative();
			throw new Error("Unreachable");
		}
		try {
			CandyObject obj = constructor.newInstance();
			obj.setCandyClass(this);
			return obj;
		} catch (CarrierErrorException e) {
			throw e;
		} catch (Exception e) {
			new NativeError(e).throwSelfNative();
		}
		throw new Error("Unreachable");
	}
	
	@Override
	public String toString() {
		return ObjectHelper.toString("build-in class", className);
	}
}
