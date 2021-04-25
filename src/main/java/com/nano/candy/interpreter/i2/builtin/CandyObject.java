package com.nano.candy.interpreter.i2.builtin;

import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;

@NativeClass(name = "Object")
public abstract class CandyObject {

	public static final boolean DEBUG = false;

	private CandyClass clazz;
	private boolean frozen;

	public CandyObject(CandyClass clazz) {
		this.clazz = clazz;
	}

	public void setCandyClass(CandyClass clazz) {
		if (this.clazz != null && DEBUG) {
			if (!clazz.isSubClassOf(this.clazz)) {
				new NativeError("Error Type.").throwSelfNative();
			}
		}
		this.clazz = clazz;
	}

	public CandyClass getCandyClass() {
		return clazz;
	}

	public boolean isCandyClass() {
		return getCandyClass() == this;
	}

	public String getCandyClassName() {
		return getCandyClass().getCandyClassName();
	}
	
	public boolean isCallable() {
		return false;
	}
	@NativeMethod(name = "isCallable")
	private CandyObject isCallable(VM vm, CandyObject[] args) {
		return BoolObj.valueOf(isCallable());
	}
	
	/**
	 * freeze() method prevents you from changing an object, thereby turning
	 * an object into a constant.
	 *
	 * <pre>
	 * After freeze an object, any attempt to modify its attributes results 
	 * in AttributeError.
	 * </pre>
	 */
	public void freeze() {
		this.frozen = true;
	}
	@NativeMethod(name = "freeze")
	private CandyObject freeze(VM vm, CandyObject[] args) {
		freeze();
		return null;
	}

	
	public boolean frozen() {
		return frozen;
	}
	@NativeMethod(name = "frozen")
	private CandyObject frozen(VM vm, CandyObject[] args) {
		return BoolObj.valueOf(frozen());
	}
	public final void checkIsFrozen() {
		if (frozen) {
			new AttributeError("The frozen object can't be changed.")
				.throwSelfNative();
		}
	}

	//  XXXApi: Usually call by VM. the method will not be executed 
	//          if it's a prototype method.
	//  XXXApiExeUser: The method will be executed if it's a prototype method.

	/**
	 * This is overloading method of the operator 'obj.attrName = value'.
	 *
	 * <pre>
	 * _setAttr(attrName, value) associates an attribute name (must be a string) with 
	 * an object.
	 *
	 * If this object is frozen, _setAttr(attrName, value) will raise an AttributeError
	 * to warn users that the object is immutable.
	 * </pre>
	 */
	public abstract void setAttrApi(VM vm, String attr, CandyObject value);
	public abstract CandyObject setAttrApiExeUser(VM vm, String attr, CandyObject value);
	public abstract CandyObject setAttr(VM vm, String attr, CandyObject value);
	@NativeMethod(name = Names.METHOD_SET_ATTR, argc = 2)
	private CandyObject setAttr(VM vm, CandyObject[] args) {
		String attr = ObjectHelper.asString(args[0]);
		checkIsFrozen();
		return setAttr(vm, attr, args[1]);
	}

	/**
	 * This is overloading method of the operator 'obj.attrName'.
	 *
	 * <pre>
	 * _getAttr(attrName) returns the value to which the specified attribute
	 * name is mapped.
	 *
	 * if the specified attribute is not found, the default implementation
	 * of the _getAttr(attrName) will invoke _getUnknownAttr(name) and return its result.
	 * </pre>
	 *
	 * @see #getAttr(VM)
	 */
	public abstract void getAttrApi(VM vm, String attr);
	public abstract CandyObject getAttrApiExeUser(VM vm, String attr);
	
	/**
	 * Returns the value to which the attribute name is mapped or null 
	 * if the specified attribute is not found.
	 */
	public abstract CandyObject getAttr(VM vm, String attr);
	
	@NativeMethod(name = Names.METHOD_GET_ATTR, argc = 1)
	private CandyObject getAttr(VM vm, CandyObject[] args) {
		String attr = ObjectHelper.asString(args[0]);
		CandyObject ret = getAttr(vm, attr);
		if (ret != null) {
			return ret;
		}
		return getUnknownAttrApiExeUser(vm, attr);
	}
	
	public abstract void getUnknownAttrApi(VM vm, String attr);
	public abstract CandyObject getUnknownAttrApiExeUser(VM vm, String attr);
	public CandyObject getUnknownAttr(VM vm, String attr) {
		AttributeError.checkAttributeNull(this, attr, null);
		throw new Error();
	}
	@NativeMethod(name = Names.METHOD_GET_UNKNOWN_ATTR, argc = 1)
	private CandyObject getUnknownAttr(VM vm, CandyObject[] args) {
		return getUnknownAttr(vm, ObjectHelper.asString(args[0]));
	}

	/**
	 * This is overloading method of the operator 'obj[key] = value'.
	 *
	 * <pre>
	 * _setItem(key, value) associates the specified key with the specified value.
	 * the key and the value can be any object.
	 * </pre>
	 */
	public abstract void setItemApi(VM vm, CandyObject key, CandyObject value);
	public abstract CandyObject setItemApiExeUser(VM vm, CandyObject key, CandyObject value);
	public CandyObject setItem(VM vm, CandyObject key, CandyObject value) {
		new TypeError(
			"'%s'['%s'] = '%s'", 
			getCandyClassName(),
			key.getCandyClassName(),
			value.getCandyClassName()
		).throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_SET_ITEM, argc = 2)
	private CandyObject setItem(VM vm, CandyObject[] args) {
		checkIsFrozen();
		setItem(vm, args[0], args[1]);
		return args[1];
	}

	
	/**
	 * This is overloading method of the operator 'obj[key]'.
	 *
	 * <pre>
	 * _getItem(key) returns the value to which the key is mapped.
	 * </pre>
	 */
	public abstract void getItemApi(VM vm, CandyObject key);
	public abstract CandyObject getItemApiExeUser(VM vm, CandyObject key);
	public CandyObject getItem(VM vm, CandyObject key) {
		new TypeError(
			"'%s'['%s']", getCandyClassName(), key.getCandyClassName()
		).throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_GET_ITEM, argc = 1)
	private CandyObject getItem(VM vm, CandyObject[] args) {
		return getItem(vm, args[0]);
	}

	
	public abstract void equalsApi(VM vm, CandyObject operand);
	public abstract BoolObj equalsApiExeUser(VM vm, CandyObject operand);
	public BoolObj equals(VM vm, CandyObject operand) {
		return BoolObj.valueOf(this == operand);
	}
	@NativeMethod(name = Names.METHOD_EQUALS, argc = 1)
	private CandyObject equals(VM vm, CandyObject[] args) {
		return equals(vm, args[0]);
	}

	
	public abstract void hashCodeApi(VM vm);
	public abstract IntegerObj hashCodeApiExeUser(VM vm);
	public IntegerObj hashCode(VM vm) {
		return IntegerObj.valueOf(super.hashCode());
	}
	@NativeMethod(name = Names.METHOD_HASH_CODE)
	private CandyObject hashCodeMethod(VM vm, CandyObject[] args) {
		return hashCode(vm);
	}


	public BoolObj not(VM vm) { 
		return boolValue(vm).not(vm); 
	}
	public BoolObj boolValue(VM vm) {	
		return BoolObj.TRUE;
	}


	/**
	 * _str() returns a string representation of an object.
	 */
	public abstract StringObj strApiExeUser(VM vm);
	public StringObj str(VM vm) {
		return StringObj.valueOf(this.toString());
	}
	@NativeMethod(name = Names.METHOD_STR_VALUE)
	private CandyObject stringValue(VM vm, CandyObject[] args) {
		return str(vm);
	}
	
	@Override
	public String toString() {
		return ObjectHelper.toString(
			getCandyClassName(), "hash - " + Integer.toHexString(hashCode())
		);
	}
	
	
	public abstract CandyObject iteratorApiExeUser(VM vm);
	public CandyObject iterator(VM vm) {
		new TypeError("the object is not iterable.")
			.throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_ITERATOR)
	private CandyObject iteratorMethod(VM vm, CandyObject[] args) {
		return iterator(vm);
	}
	
	
	@NativeMethod(name = Names.METHOD_INITALIZER)
	private CandyObject objDefaultInitializer (VM vm, CandyObject[] args) { return this; }

	@NativeMethod(name = "_class")
	private CandyObject getClass(VM vm, CandyObject[] args) {
		return getCandyClass();
	}


	public abstract void positiveApi(VM vm);
	public abstract CandyObject positiveApiExeUser(VM vm);
	public CandyObject positive(VM vm) {
		new TypeError("+").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_POSITIVE)
	private CandyObject positiveMethod(VM vm, CandyObject[] args) {
		return positive(vm);
	}

	public abstract void negativeApi(VM vm);
	public abstract CandyObject negativeApiExeUser(VM vm);
	public CandyObject negative(VM vm) {
		new TypeError("-").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_NEGATIVE)
	private CandyObject negativeMethod(VM vm, CandyObject[] args) {
		return negative(vm);
	}

	public abstract void addApi(VM vm, CandyObject operand);
	public abstract CandyObject addApiExeUser(VM vm, CandyObject operand);
	public CandyObject add(VM vm, CandyObject operand) {
		if (operand instanceof StringObj || this instanceof StringObj) {
			return strApiExeUser(vm).add(vm, operand);
		}
		new TypeError("+").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_ADD, argc = 1)
	private CandyObject addMethod(VM vm, CandyObject[] args) {
		return add(vm, args[0]);
	}

	public abstract void subApi(VM vm, CandyObject operand);
	public abstract CandyObject subApiExeUser(VM vm, CandyObject operand);
	public CandyObject sub(VM vm, CandyObject operand) {
		new TypeError("-").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_SUB, argc = 1)
	private CandyObject subMethod(VM vm, CandyObject[] args) {
		return sub(vm, args[0]);
	}

	public abstract void mulApi(VM vm, CandyObject operand);
	public abstract CandyObject mulApiExeUser(VM vm, CandyObject operand);
	public CandyObject mul(VM vm, CandyObject operand) {
		new TypeError("*").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_MUL, argc = 1)
	private CandyObject mulMethod(VM vm, CandyObject[] args) {
		return mul(vm, args[0]);
	}

	public abstract void divApi(VM vm, CandyObject operand);
	public abstract CandyObject divApiExeUser(VM vm, CandyObject operand);
	public CandyObject div(VM vm, CandyObject operand) {
		new TypeError("/").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_DIV, argc = 1)
	private CandyObject divMethod(VM vm, CandyObject[] args) {
		return div(vm, args[0]);
	}

	public abstract void modApi(VM vm, CandyObject operand);
	public abstract CandyObject modApiExeUser(VM vm, CandyObject operand);
	public CandyObject mod(VM vm, CandyObject operand) {
		new TypeError("%").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_MOD, argc = 1)
	private CandyObject modMethod(VM vm, CandyObject[] args) {
		return mod(vm, args[0]);
	}

	public abstract void gtApi(VM vm, CandyObject operand);
	public abstract BoolObj gtApiExeUser(VM vm, CandyObject operand);
	public BoolObj gt(VM vm, CandyObject operand) {
		new TypeError(">").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_GT, argc = 1)
	private CandyObject gtMethod(VM vm, CandyObject[] args) {
		return gt(vm, args[0]);
	}

	public abstract void gteqApi(VM vm, CandyObject operand);
	public abstract BoolObj gteqApiExeUser(VM vm, CandyObject operand);
	public BoolObj gteq(VM vm, CandyObject operand) {
		new TypeError(">=").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_GTEQ, argc = 1)
	private CandyObject gteqMethod(VM vm, CandyObject[] args) {
		return gteq(vm, args[0]);
	}

	public abstract void ltApi(VM vm, CandyObject operand);
	public abstract BoolObj ltApiExeUser(VM vm, CandyObject operand);
	public BoolObj lt(VM vm, CandyObject operand) {
		new TypeError("<").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_LT, argc = 1)
	private CandyObject ltMethod(VM vm, CandyObject[] args) {
		return lt(vm, args[0]);
	}

	public abstract void lteqApi(VM vm, CandyObject operand);
	public abstract BoolObj lteqApiExeUser(VM vm, CandyObject operand);
	public BoolObj lteq(VM vm, CandyObject operand) {
		new TypeError("<=").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_LTEQ, argc = 1)
	private CandyObject lteqMethod(VM vm, CandyObject[] args) {
		return lteq(vm, args[0]);
	}

}
