package com.nano.candy.interpreter.i1.builtin.type;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.classes.ObjectClass;
import com.nano.candy.interpreter.i1.builtin.func.BuiltinMethodHelper;
import com.nano.candy.interpreter.i1.builtin.func.CandyClassMethods;
import com.nano.candy.interpreter.i1.builtin.type.NullPointer;
import java.util.HashMap;
import java.util.Objects;

/**
 * All objects have a class(a class is also an object, and its class is itself).
 *
 * The class in Candy language is desgined as a callable object, that's is an
 * elegant way to implement the Class mechanism.
 *
 * Note that every Class object is unique instance.
 */
public class CandyClass extends CandyObject implements Callable {

	private String className;
	private CandyClass superClass;
	private boolean inheritable;
	
	/**
	 * All defined methods of this class.
	 */
	private HashMap<String, Callable> methods;
	
	protected Callable initializer;
	
	/**
	 * CandyClass own methods.
	 */
	private CandyClassMethods ownMethods;
	
	public CandyClass(String className) {
		this(className, ObjectClass.getInstance());
	}
	
	public CandyClass(String className, CandyClass superClass) {
		this(className, superClass, null);
	}
	
	public CandyClass(String className, CandyClass superClass, Callable initializer) {
		this(className, superClass, new HashMap<>(), initializer);
	}

	public CandyClass(String className,
	                  CandyClass superClass,
	                  HashMap<String, Callable> definedMethods, 
					  Callable initializer) {
		super(null);
		this.className = className;
		this.superClass = superClass;
		this.methods = definedMethods;
		this.initializer = initializer;
		this.inheritable = true;
		this.ownMethods = new CandyClassMethods(this);
		freeze();
	}
	
	public Callable getInitializer() {
		if (initializer == null) {
			initializer = findInitializer();
		}
		return initializer;
	}
	
	private Callable findInitializer() {
		CandyClass klass = superClass;
		while (klass != null && initializer == null) {
			initializer = klass.initializer;
			klass = klass.superClass; 
		}
		if (initializer == null) {
			initializer = BuiltinMethodHelper.EMPTY_INTIALIZER;
		}
		return initializer;
	}
	
	protected void defineMethod(String name, Callable callable) {
		methods.put(name, callable);
	}

	public Callable findMethod(String methodName) {
		Callable func = methods.get(methodName);
		if (func == null) {
			CandyClass _class = superClass;
			while (func == null && _class != null) {
				func = _class.methods.get(methodName);
				_class = _class.superClass;
			}
		}
		return func;
	}
	
	/**
	 * Make this class uninheritable.
	 * Most built-in classes are uninheritable.
	 */
	public void makeUninheritable() {
		inheritable = false;
	}
	
	public boolean isInheritable() {
		return inheritable;
	}
	
	public String getClassName() {
		return className;
	}
	
	public boolean isSubclassOf(CandyClass klass) {
		CandyClass c = this;
		while (c != null && c != klass) {
			c = c.superClass;
		}
		return c != null;
	}
	
	public boolean isSuperclassOf(CandyClass klass) {
		CandyClass c = klass;
		while (c != null && c != this) {
			c = c.superClass;
		}
		return c != null;
	}
	
	public String getMethodMapStr() {
		return methods.toString();
	}

	@Override
	public CandyClass _class() {
		return this;
	}

	@Override
	public CandyObject getAttr(String attr) {
		switch (attr) {
			case "_super": 
				return Objects.requireNonNullElse(superClass, NullPointer.nil());
			case "name": 
				return StringObject.of(className);
			case "isSubclassOf":
				return ownMethods.isSubClassOf();
			case "isSuperclassOf": 
				return ownMethods.isSuperClassOf();
		}
		return null;
	}
	
	@Override
	public StringObject stringValue() {
		return StringObject.of("<meta class: " + getClassName() + ">") ;
	}

	@Override
	public int arity() {
		return getInitializer().arity();
	}

	@Override
	public Callable bindToInstance(CandyObject instance) {
		// Shouldn't reach here.
		throw new Error("Binding an instance is not supported..");
	}
	
	@Override
	public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
		CandyObject instance = new CandyObject(this);
		getInitializer().bindToInstance(instance).onCall(interpreter, args);
		return instance;
	}
}
