package com.nano.candy.interpreter.i2.builtin;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.MethodObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import com.nano.candy.interpreter.i2.cni.FasterNativeMethod;
import com.nano.candy.interpreter.i2.runtime.OperandStack;
import com.nano.candy.interpreter.i2.runtime.StackFrame;
import com.nano.candy.std.Names;
import java.util.Collection;
import java.util.HashMap;

/**
 * A candy class provides a language level class object.
 *
 * It's also a callable object. You call it just like calling a
 * function that can return an instance of this class.
 */
public class CandyClass extends CallableObj {
	
	private static ParametersInfo genParamtersInfo(CallableObj initalizer) {
		return new ParametersInfo(
			// The initalizer takes an extra argument.
			// We will call the initalizer when this call is called and
			// pass a new instance to the initalizer.
			initalizer == null ? 0 : initalizer.arity()-1,
			initalizer == null ? -1 : initalizer.vaargIndex()
		);
	}
	
	/**
	 * Use ReflectASM to call reflectly the no-args constructor of CandyObject
	 */
	protected final ConstructorAccess<? extends CandyObject> constructorAccess;

	/**
	 * False if the object class (java class) has no a constructor 
	 * with no-args or the constructor is private or protected.
	 *
	 * This class can't create instances if false.
	 */
	protected final boolean canBeCreated;
	
	/**
	 * You can't interit this class from the Candy language level if false.
	 *
	 * Some built-in classes don't allow themselves to be inherited by the
	 * prototype class(wrote by the Candy programer).
	 */
	protected final boolean isInheritable;

	protected final CandyClass superClass;
	protected final String className;

	protected final HashMap<String, CallableObj> methods;
	protected final CallableObj initializer;
	
	private FasterNativeMethod isSubclassOf, 
	                           isSuperclassOf,
	                           instance,
	                           methodsMethod;

	protected CandyClass(ClassSignature signature) {
		super(signature.className, genParamtersInfo(signature.initializer));
		this.superClass = signature.superClass;
		this.className = signature.className;
		this.isInheritable = signature.isInheritable;
		this.methods = signature.methods;
		this.initializer = signature.initializer;
		this.constructorAccess = signature.constructorAccess;
		this.canBeCreated = signature.canBeCreated;
		
		if (this.initializer != null) {
			methods.put(Names.METHOD_INITALIZER, initializer);
		}
	}

	@Override
	protected CandyClass initSelfCandyClass() {
		return CallableObj.getCallableClass();
	}

	public Collection<CallableObj> getMethods() {
		return methods.values();
	}

	public CallableObj getMethod(String name) {
		return methods.get(name);
	}

	/**
	 * Returns a method bound with the specified instance. The method
	 * is executable and the specified instance is treated as the first
	 * argument of the method to be passed when the method is called.
	 */
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

	public final String getName() {
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
	
	/**
	 * This is the native method provided for Candy, which is used to
	 * return whather this class is the subclass of the given class.
	 *
	 * Prototype: isSubclassOf(klass)
	 */
	private final CandyObject isSubclassOf(CNIEnv env, CandyObject[] args) {
		CandyClass klass = TypeError.requiresClass(args[0]);
		return BoolObj.valueOf(isSubClassOf(klass));
	}
	
	/**
	 * This is the native method provided for Candy, which is used to
	 * return whather this class is the superclass of the given class.
	 *
	 * Prototype: isSuperclassOf(klass)
	 */
	private final CandyObject isSuperclassOf(CNIEnv env, CandyObject[] args) {
		CandyClass klass = TypeError.requiresClass(args[0]);
		return BoolObj.valueOf(isSuperClassOf(klass));
	}
	
	/**
	 * This is the native method provided for Candy, which is used to
	 * return whather the given object is instance of this class;
	 *
	 * Prototype: instance(object)
	 */
	private final CandyObject instance(CNIEnv env, CandyObject[] args) {
		CandyClass klz = args[0].getCandyClass();
		return BoolObj.valueOf(isSubClassOf(klz));
	}
	
	/**
	 * This is the native method provided for Candy, which is used to
	 * return the array that contains all method names of this class.
	 *
	 * Prototype: methods()
	 */
	private final CandyObject methods(CNIEnv env, CandyObject[] args) {
		ArrayObj arr = new ArrayObj(methods.size());
		for (String name : methods.keySet()) {
			arr.append(StringObj.valueOf(name));
		}
		return arr;
	}

	@Override
	public CandyObject getAttr(CNIEnv env, String name) {
		switch (name) {
			case "className": 
				return StringObj.valueOf(className);
			case "superClass": 
				return superClass == null ? NullPointer.nil() : superClass;
			case "isSubclassOf":
				if (isSubclassOf == null) {
					isSubclassOf = new FasterNativeMethod(
						getName(), "isSubclassOf", 1, this::isSubclassOf
					);
				}
				return isSubclassOf;
			case "isSuperclassOf":
				if (isSuperclassOf == null) {
					isSuperclassOf = new FasterNativeMethod(
						getName(), "isSuperclassOf", 1, this::isSuperclassOf
					);
				}
				return isSuperclassOf;
			case "methods":
				if (methodsMethod == null) {
					methodsMethod = new FasterNativeMethod(
						getName(), "methods", 0, this::methods
					);
				}
				return methodsMethod;
			case "instance":
				if (instance == null) {
					instance = new FasterNativeMethod(
						getName(), "instance", 1, this::instance
					);
				}
				return instance;
		}
		return super.getAttr(env, name);
	}

	@Override
	protected boolean isBuiltinAttribute(String name) {
		switch(name) {
			case "className": 
			case "superClass":
			case "isSubclassOf":
			case "isSuperclassOf":
			case "methods":
			case "instance":
				return true;
		}
		return super.isBuiltinAttribute(name);
	}
	
	@Override
	public boolean isBuiltin() {
		return initializer == null ? true : initializer.isBuiltin();
	}

	@Override
	public void onCall(CNIEnv env, OperandStack opStack, StackFrame stack, int argc, int unpackFlags) {
		CandyObject instance = createInstance(env);
		new MethodObj(instance, initializer).onCall(
			env, opStack, stack, argc, unpackFlags
		);
	}

	protected CandyObject createInstance(CNIEnv env) {		
		if (!canBeCreated) {
			new NativeError(
				"The built-in class can't be instantiated: " 
				+ getName()
			).throwSelfNative();
		}
		if (constructorAccess == null) {
			return new CandyObject(this);
		}
		// We can't reflectly call the constructor with a CandyClass
		// due to the restriction of ReflectASM.
		CandyObject obj = constructorAccess.newInstance();
		obj.setCandyClass(this);
		return obj;
	}

	@Override
	protected String strTag() {
		return "class";
	}
}
