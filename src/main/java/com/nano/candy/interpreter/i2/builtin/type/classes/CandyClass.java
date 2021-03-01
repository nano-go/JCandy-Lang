package com.nano.candy.interpreter.i2.builtin.type.classes;
import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObjEntity;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.MethodObj;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import java.util.HashMap;

public class CandyClass extends BuiltinObject {
	
	private CandyClass superClass;
	private String className;
	
	private HashMap<String, CallableObj> methods;
	protected CallableObj initializer;
	
	public CandyClass(String name) {
		this(name, ObjectClass.getObjClass());
	}
	
	public CandyClass(String name, CandyClass superClass) {
		super(null);
		this.className = name;
		this.superClass = superClass;
		if (superClass != null) {
			this.methods = new HashMap<String, CallableObj>(superClass.methods);
			this.initializer = superClass.initializer;
		} else {
			this.methods = new HashMap<>();
		}
	}
	
	public void setInitalizer(CallableObj initalizer) {
		this.initializer = initalizer;
	}
	
	public CallableObj getMethod(String name) {
		CallableObj method = methods.get(name);
		if (method == null) {
			if (Names.METHOD_INITALIZER.equals(name) &&
			    initializer != null) {
				return initializer;
			}
		}
		return method;
	}
	
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
	public int arity() {
		return initializer == null ? 0 : initializer.arity()-1;
	}

	@Override
	public boolean isCallable() {
		return true;
	}

	@Override
	public void onCall(VM vm) {
		CandyObject instance = new CandyObjEntity(this);
		vm.push(instance);
		if (initializer != null) {
			initializer.onCall(vm);
		}
	}
	
	@Override
	public String toString() {
		return ObjectHelper.toString(
			"build-in class", className
		);
	}
}
