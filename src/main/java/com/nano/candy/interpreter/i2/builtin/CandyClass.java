package com.nano.candy.interpreter.i2.builtin;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.MethodObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
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

	/**
	 * See Constructor.
	 */
	private static ParametersInfo genParamtersInfo(CallableObj initalizer) {
		return new ParametersInfo(
			initalizer == null ? 0 : initalizer.arity()-1,
			initalizer == null ? -1 : initalizer.varArgsIndex()-1
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

	protected CandyClass(ClassSignature signature) {
		super(null, signature.className, genParamtersInfo(signature.initializer));
		this.setCandyClass(this);
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

	@Override
	public CandyObject getAttr(CNIEnv env, String name) {
		switch (name) {
			case "className": 
				return StringObj.valueOf(className);
			case "superClass": 
				return superClass == null ? NullPointer.nil() : superClass;
		}
		return super.getAttr(env, name);
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
	public void onCall(CNIEnv env, OperandStack opStack, StackFrame stack, int argc, int unpackFlags) {
		CandyObject instance = createInstance(env);
		new MethodObj(instance, initializer).onCall(
			env, opStack, stack, argc, unpackFlags
		);
	}
	
	@Override
	public void call(CNIEnv env, int argc, int unpackFlags) {
		CandyObject instance = createInstance(env);
		new MethodObj(instance, initializer).call(env, argc, unpackFlags);
	}

	@Override
	public CandyObject callExeUser(CNIEnv env, int unpackFlags, CandyObject[] args) {
		CandyObject instance = createInstance(env);
		return new MethodObj(instance, initializer).callExeUser(env, unpackFlags, args);
	}

	protected CandyObject createInstance(CNIEnv env) {		
		if (!canBeCreated) {
			new NativeError(
				"The built-in class can't be instantiated: " 
				+ getCandyClassName()
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
