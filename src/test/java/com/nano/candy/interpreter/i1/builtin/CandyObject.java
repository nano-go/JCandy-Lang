package com.nano.candy.interpreter.i1.builtin;
import com.nano.candy.interpreter.error.AttributeError;
import com.nano.candy.interpreter.error.TypeError;
import com.nano.candy.interpreter.error.UnsupportedOperationError;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.classes.ObjectClass;
import com.nano.candy.interpreter.i1.builtin.classes.StringClass;
import com.nano.candy.interpreter.i1.builtin.func.BuiltinMethodObj;
import com.nano.candy.interpreter.i1.builtin.func.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i1.builtin.type.BooleanObject;
import com.nano.candy.interpreter.i1.builtin.type.Callable;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;
import com.nano.candy.interpreter.i1.builtin.type.IntegerObject;
import com.nano.candy.interpreter.i1.builtin.type.StringObject;
import com.nano.candy.parser.TokenKind;
import java.util.HashMap;
import java.util.Objects;

/**
 * The super class of all objects in candy.
 */
public class CandyObject {
	
	private HashMap<String, CandyObject> attrs;
	
	/**
	 * Every time a method is called, object need to find this method and
	 * bind it(this operation will create a new method).
	 *
	 * For optimal performance, we use cache to query faster.
	 */
	private HashMap<String, Callable> methodCache;
	
	private CandyClass _class;
	
	/**
	 * If true, attributes('x.y = a') and items('x[y] = a') can't be changed.
	 */
	private boolean frozen;
	
	/* faster cache */
	private Callable attrGetter;
	private Callable attrSetter;
	private Callable itemGetter;
	private Callable itemSetter;
	private Callable boolVal;
	private Callable strVal;
	
	public CandyObject() {
		this(null);
	}
	
	public CandyObject(CandyClass _class) {
		this.attrs = new HashMap<>();
		this.methodCache = new HashMap<>();
		
		// CandyClass is also CandyObject.
		// CandyClass._class() is itself.
		if (this instanceof CandyClass) {
			this._class = (CandyClass) this;
		} else {
			this._class = Objects.requireNonNullElse(
				_class, ObjectClass.getInstance()
			);
		}
	}
	
	protected final Callable findMethodIfNull(Callable method, String name) {
		if (method != null) {
			return method;
		}
		return findMethodAndBinds(name, false);
	}
	
	protected final Callable findMethodAndBinds(String methodName, boolean cache) {
		Callable method = _class().findMethod(methodName);
		if (method != null) {
			Callable boundMethod = method.bindToInstance(this);
			if (cache){
				methodCache.put(methodName, boundMethod);	
			}
			return boundMethod;
		}
		return null;
	}

	public CandyClass _class() {
		return _class;
	}
	
	public String getClassName() {
		return _class().getClassName();
	}
	
	public boolean instanceOf(CandyClass clazz) {
		return _class().isSubclassOf(clazz);
	}
	
	public void freeze() {
		this.frozen = true;
	}

	public boolean frozen() {
		return frozen;
	}
	
	protected void throwFrozenObjError() {
		throw new AttributeError("The frozen object can't be changed.");
	}
	
	private final void checkIsFrozen() {
		if (frozen) {
			throwFrozenObjError();
		}
	}
	
	/**
	 * Operator: 'x.y = alpha'. Method: '_setAttr(name, value)'
	 *
	 * <p>
	 * When the {@code Interpreter} executes an expression like {@code 'x.y = a'} will
	 * set a property of this object by this method.
	 * </p>
	 */
	public CandyObject setAttr(AstInterpreter interpreter, String attr, CandyObject value) {
		checkIsFrozen();
		attrSetter = findMethodIfNull(attrSetter, "_setAttr");
		if (attrSetter instanceof BuiltinMethodObj) {
			return setAttr(attr, value);
		}
		return CandyHelper.invoke(
			interpreter, attrSetter, StringObject.of(attr), value);
	}
	
	public CandyObject setAttr(String attr, CandyObject ref) {
		attrs.put(attr, ref);
		return ref;
	}
	
	/**
	 * Operator: 'x.y'. Method: '_getAttr(name)'
	 *
	 * <p>
	 * When the {@code Interpreter} executes an expression like {@code 'x.y'} will
	 * get a property of this object by this method.
	 * </p>
	 */
	public CandyObject getAttr(AstInterpreter interpreter, String attr) {
		attrGetter = findMethodIfNull(attrGetter, "_getAttr");
		if (attrGetter instanceof BuiltinMethodObj) {
			return getAttr(attr);
		}
		return CandyHelper.invoke(
			interpreter, attrGetter, StringObject.of(attr));
	}
	
	public CandyObject getAttr(String attr) {
		CandyObject val = attrs.get(attr);
		if (val != null) {
			return val;
		}
		Callable method = methodCache.get(attr);
		if (method != null) {
			return (CandyObject) method;
		}
		return (CandyObject) findMethodAndBinds(attr, true);
	}
	
	public CandyObject delAttr(String attr) {
		CandyObject val = attrs.get(attr);
		if (val != null) {
			attrs.remove(attr);
			return val;
		}
		
		Callable method = methodCache.get(attr);
		if (method == null) {
			method = findMethodAndBinds(attr, true);
		}
		if (method == null) {
			AttributeError.requiresAttrNonNull(_class(), attr, (CandyObject) method);
		}
		throw new AttributeError(
			"The '%s' object defined method '%s' can't be deleted.", 
			getClassName(), attr
		);
	}
	
	/**
	 * Operator: 'x[y] = alpha'. Method: '_setItem(key, value)'
	 *
	 * <p>
	 * When the {@code Interpreter} executes an expression like {@code x[y] = a} will
	 * associates the specified value with the specified key by this method.
	 * </p>
	 *
	 * <p>
	 * Note that the implementation of the operator 'x[y] = alpha' is not supported.
	 * </p>
	 */
	public CandyObject setItem(AstInterpreter interpreter, CandyObject key, CandyObject value) {
		checkIsFrozen();
		itemSetter = findMethodIfNull(itemSetter, "_setItem");
		if (itemSetter instanceof BuiltinMethodObj) {
			return setItem(key, value);
		}
		return CandyHelper.invoke(interpreter, itemSetter, key, value);
	}

	public CandyObject setItem(CandyObject key, CandyObject value) {
		throw new UnsupportedOperationError("'[%s]'", key.getClassName());
	}
	
	/**
	 * operator: 'x[y]'. Method: _getItem()
	 *
	 * <p>
	 * When the {@code Interpreter} executes an expression like {@code x[y]} will
	 * get an item to which the key is mapped by this method.
	 * </p>
	 *
	 * <p>
	 * Note that the implementation of the operator 'x[y]' is not supported.
	 * </p>
	 */
	public CandyObject getItem(AstInterpreter interpreter, CandyObject key) {
		itemGetter = findMethodIfNull(itemGetter ,"_getItem");
		if (itemGetter instanceof BuiltinMethodObj) {
			return getItem(key);
		}
		return CandyHelper.invoke(interpreter, itemGetter, key);
	}

	public CandyObject getItem(CandyObject key) {
		throw new UnsupportedOperationError("'[%s]'", key.getClassName());
	}
	
	public CandyObject delItem(CandyObject key) {
		throw new UnsupportedOperationError("delete item: [%s]", key.getClassName());
	}
	
	public CandyObject negative() {
		throw new UnsupportedOperationError(TokenKind.MINUS, this);
	}
	
	public CandyObject positive() {
		throw new UnsupportedOperationError(TokenKind.PLUS, this);
	}
	
	public BooleanObject not(AstInterpreter interpreter) {
		return BooleanObject.valueOf(!booleanValue(interpreter).value());
	}

	public CandyObject plus(AstInterpreter interpreter, CandyObject obj) {
		if (this instanceof StringObject || obj instanceof StringObject) {
			return StringObject.of(
				stringValue(interpreter).value() + obj.stringValue(interpreter).value()
			) ;
		}
		throw new UnsupportedOperationError(TokenKind.PLUS, this, obj);
	}

	public CandyObject subtract(CandyObject obj) {
		throw new UnsupportedOperationError(TokenKind.MINUS, this, obj);
	}

	public CandyObject times(CandyObject obj) {
		throw new UnsupportedOperationError(TokenKind.STAR, this, obj);
	}

	public CandyObject divide(CandyObject obj) {
		throw new UnsupportedOperationError(TokenKind.DIV, this, obj);
	}
	
	public CandyObject mod(CandyObject obj) {
		throw new UnsupportedOperationError(TokenKind.MOD, this, obj);
	}
	
	public BooleanObject greaterThan(CandyObject obj) {
		throw new UnsupportedOperationError(TokenKind.GT, this, obj);
	}

	public BooleanObject greaterThanOrEqualTo(CandyObject obj) {
		try {
			if (greaterThan(obj).value()) {
				return BooleanObject.TRUE;
			}
			return equalTo(obj) ;
		} catch (UnsupportedOperationError e) {
			throw new UnsupportedOperationError(TokenKind.GTEQ, this, obj);
		}
	}

	public BooleanObject lessThan(CandyObject obj) {
		throw new UnsupportedOperationError(TokenKind.LT, this, obj);
	}

	public BooleanObject lessThanOrEqualTo(CandyObject obj) {
		try {
			if (lessThan(obj).value()) {
				return BooleanObject.TRUE;
			}
			return equalTo(obj);
		} catch (UnsupportedOperationError e) {
			throw new UnsupportedOperationError(TokenKind.LTEQ, this, obj);
		}
	}
	
	public BooleanObject notEqualTo(CandyObject obj) {
		return equalTo(obj).not();
	}
	
	public BooleanObject equalTo(CandyObject obj) {
		return BooleanObject.valueOf(this == obj);
	}

	public BooleanObject booleanValue(AstInterpreter interpreter) {	
		boolVal = findMethodIfNull(boolVal, "_bool");
		if (boolVal instanceof BuiltinMethodObj) {
			return booleanValue();
		}
		return CandyHelper.invoke(interpreter, boolVal).booleanValue();
	}
	
	
	public BooleanObject booleanValue() {
		return BooleanObject.TRUE;
	}
	
	public StringObject stringValue(AstInterpreter interpreter) {
		strVal = findMethodIfNull(strVal, "_str");	
		if (strVal instanceof BuiltinMethodObj) {
			return stringValue();
		}
		return CandyHelper.invoke(interpreter, strVal).stringValue();
		
	}
	
	public StringObject stringValue() {
		return StringObject.of("<class: " + getClassName() + ">");
	}
	
	/**
	 * Method: '_iterator'
	 *
	 * <p>
	 * When the {@code Interpreter} executes 'FOR IN ITERABLE' statement will
	 * get an iterator from an object by this method to iterate.
	 * </p>
	 */
	public CandyObject iterator(AstInterpreter interpreter) {
		Callable _iterator = findMethodAndBinds("_iterator", true);
		if (_iterator != null) {
			return CandyHelper.invoke(interpreter, _iterator);
		}
		return iterator();
	}
	
	public CandyObject iterator() {
		throw new TypeError("'%s' object is not iterable.", getClassName());
	}
	
	public int compareTo(CandyObject obj) {
		if (greaterThan(obj).value()) {
			return 1;
		}
		if (equalTo(obj).value()) {
			return 0;
		}
		return -1;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CandyObject) {
			return equalTo((CandyObject)obj).value();
		}
		return super.equals(obj);
	}
	
	/*===================== Built-in Methods ===================*/

	@BuiltinMethod("_hashcode")
	public CandyObject hashCode(AstInterpreter interpreter, CandyObject[] args) {
		return new IntegerObject(hashCode());
	}

	@BuiltinMethod(value = "_getAttr", argc = 1)
	public CandyObject getAttr(AstInterpreter interpreter, CandyObject[] args) {
		TypeError.checkTypeMatched(StringClass.STRING_CLASS, args[0]);
		String attrName = ((StringObject)args[0]).value();
		CandyObject value = getAttr(attrName);
		AttributeError.requiresAttrNonNull(_class(), attrName, value);
		return value;
	}

	@BuiltinMethod(value = "_setAttr", argc = 2)
	public CandyObject setAttr(AstInterpreter interpreter, CandyObject[] args) {
		TypeError.checkTypeMatched(StringClass.STRING_CLASS, args[0]);
		String attrName = ((StringObject)args[0]).value();
		return setAttr(attrName, args[1]);
	}
	
	@BuiltinMethod(value = "_delAttr", argc = 1)
	public CandyObject delAttr(AstInterpreter interpreter, CandyObject[] args) {
		TypeError.checkTypeMatched(StringClass.STRING_CLASS, args[0]);
		return delAttr(((StringObject)args[0]).value());
	}

	@BuiltinMethod(value = "_getItem", argc = 1)
	public CandyObject getItem(AstInterpreter interpreter, CandyObject[] args) {
		return getItem(interpreter, args[0]);
	}

	@BuiltinMethod(value = "_setItem", argc = 2)
	public CandyObject setItem(AstInterpreter interpreter, CandyObject[] args) {
		return setItem(interpreter, args[0], args[1]);
	}

	@BuiltinMethod(value = "_delItem", argc = 1)
	public CandyObject delItem(AstInterpreter interpreter, CandyObject[] args) {
		return delItem(args[0]);
	}
	
	@BuiltinMethod("_class")
	public CandyObject _class(AstInterpreter interpreter, CandyObject[] args) {
		return _class();
	}

	@BuiltinMethod("_str")
	public CandyObject strVal(AstInterpreter interpreter, CandyObject[] args) {
		return stringValue();
	}

	@BuiltinMethod("_bool")
	public CandyObject boolVal(AstInterpreter interpreter, CandyObject[] args) {
		return booleanValue();
	}
	
	@BuiltinMethod("_iterator")
	public CandyObject iterator(AstInterpreter interpreter, CandyObject[] args) {		
		return iterator();
	}
	
	@BuiltinMethod("freeze")
	public CandyObject freeze(AstInterpreter interpreterq, CandyObject[] args) {
		freeze();
		return this;
	}

	@BuiltinMethod("frozen")
	public CandyObject frozen(AstInterpreter interpreter, CandyObject[] args) {		
		return BooleanObject.valueOf(frozen());
	}

}
