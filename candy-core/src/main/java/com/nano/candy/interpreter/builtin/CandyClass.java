package com.nano.candy.interpreter.builtin;

import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.BoolObj;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.MethodObj;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.builtin.type.error.NativeError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.JavaFunctionObj;
import com.nano.candy.interpreter.runtime.OperandStack;
import com.nano.candy.std.CandyAttrSymbol;
import com.nano.candy.std.Names;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * A candy class provides a language level class object.
 *
 * It's also a callable object. You call it just like calling a
 * function that can return an instance of this class.
 */
public class CandyClass extends CallableObj {
	
	/**
	 * We use the non-args constructor to create various objects.
	 *
	 * <p>Every candy class corresponds with an original Java class, and we 
	 * need the constructor of the Java class to create various Candy 
	 * objects. If the Java class has no such constructors, it means the
	 * corresponding Candy class can't create instances.
	 *
	 * <p>We create Candy objects by reflection.
	 *
	 * <p>Note that only public or protected non-args constructors can be 
	 * considered as an object allocator.
	 */
	protected final Constructor<? extends CandyObject> objectAllocator;

	/**
	 * False if there is no a constructor with non-args.
	 *
	 * <p>This class can not be instantiated if false.
	 */
	protected final boolean canBeInstantiated;
	
	/**
	 * You can't interit this class from Candy language level if false.
	 *
	 * <p>Some built-in classes don't allow themselves to be inherited,
	 * such as {@code Module}, {@code Callable}...
	 */
	protected final boolean isInheritable;
	
	/**
	 * The superclass of this. Nullable.
	 */
	protected final CandyClass superClass;
	
	/**
	 * The declared class name.
	 */
	protected final String className;
	
	/**
	 * The definied methods.
	 */
	protected final HashMap<String, CallableObj> methods;
	
	/**
	 * Those are predefined attributes with modifiers
	 *
	 * <pre>
	 * class Foo {
	 *     pri a, b, c
	 * }
	 * </pre>
	 *
	 * <p> In above class, {@code a, b, c} are predefined attributes 
	 * with the {@code private('pri')} modifier.
	 */
	protected final Set<CandyAttrSymbol> predefinedAttrs;
	
	/**
	 * The initializer of this class. Nullable.
	 */
	protected final CallableObj initializer;
	
	protected CandyClass(ClassSignature signature) {
		super(signature.className, 
			  signature.initializer == null ? 0 : signature.initializer.arity()-1,
			  signature.initializer == null ? -1 : signature.initializer.vaargIndex());
		this.predefinedAttrs = signature.attrs;
		this.superClass = signature.superClass;
		this.className = signature.className;
		this.isInheritable = signature.isInheritable;
		this.methods = signature.methods;
		this.initializer = signature.initializer;
		this.objectAllocator = signature.objectAllocator;
		this.canBeInstantiated = signature.canBeInstantiated;
		
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
	private final CandyObject isSubclassOf(CNIEnv env, OperandStack opStack) {
		CandyClass klass = TypeError.requiresClass(opStack.pop());
		return BoolObj.valueOf(isSubClassOf(klass));
	}
	
	/**
	 * This is the native method provided for Candy, which is used to
	 * return whather this class is the superclass of the given class.
	 *
	 * Prototype: isSuperclassOf(klass)
	 */
	private final CandyObject isSuperclassOf(CNIEnv env, OperandStack opStack) {
		CandyClass klass = TypeError.requiresClass(opStack.pop());
		return BoolObj.valueOf(isSuperClassOf(klass));
	}
	
	/**
	 * This is the native method provided for Candy, which is used to
	 * return whather the given object is instance of this class;
	 *
	 * Prototype: instance(object)
	 */
	private final CandyObject instance(CNIEnv env, OperandStack opStack) {
		CandyClass klz = opStack.pop().getCandyClass();
		return BoolObj.valueOf(isSubClassOf(klz));
	}
	
	/**
	 * This is the native method provided for Candy, which is used to
	 * return the array that contains all method names of this class.
	 *
	 * Prototype: methods()
	 */
	private final CandyObject methods(CNIEnv env, OperandStack opStack) {
		ArrayObj arr = new ArrayObj(methods.size());
		for (String name : methods.keySet()) {
			arr.append(StringObj.valueOf(name));
		}
		return arr;
	}
	
	@Override
	protected void initAttrs() {
		super.initAttrs();
		setBuiltinMetaData("className", StringObj.valueOf(className));
		setBuiltinMetaData("superClass", 
			superClass == null ? NullPointer.nil() : superClass);
		defineJavaFunction(new JavaFunctionObj(
			getName(), "isSubclassOf", 1, this::isSubclassOf));
		defineJavaFunction(new JavaFunctionObj(
			getName(), "isSuperclassOf", 1, this::isSuperclassOf));
		defineJavaFunction(new JavaFunctionObj(
			getName(), "methods", 0, this::methods));
		defineJavaFunction(new JavaFunctionObj(
			getName(), "instance", 1, this::instance));
	}
	
	private final void defineJavaFunction(JavaFunctionObj fn) {
		setBuiltinMetaData(fn.funcName(), fn);
	}
	
	@Override
	public boolean isBuiltin() {
		return initializer == null ? true : initializer.isBuiltin();
	}

	@Override
	public void onCall(CNIEnv env, OperandStack opStack, int argc, int unpackFlags) {
		CandyObject instance = createInstance(env);
		new MethodObj(instance, initializer).onCall(
			env, opStack, argc, unpackFlags
		);
	}

	protected CandyObject createInstance(CNIEnv env) {		
		if (!canBeInstantiated) {
			new NativeError(
				"The built-in class can't be instantiated: " 
				+ getName()
			).throwSelfNative();
		}
		if (objectAllocator == null) {
			return new CandyObject(this).addAttrs(predefinedAttrs);
		}
		try {
			CandyObject obj = objectAllocator.newInstance();
			obj.setCandyClass(this);
			return obj.addAttrs(predefinedAttrs);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException) e.getTargetException();
			}
			new NativeError(e.getTargetException()).throwSelfNative();
		} catch (IllegalArgumentException | 
		         InstantiationException | 
				 IllegalAccessException e) {
			// Unreachable.
		}
		throw new RuntimeException("Unreachable.");
	}
	
	@Override
	protected String strTag() {
		return "class";
	}
}
