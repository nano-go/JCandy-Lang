package com.nano.candy.interpreter.i2.builtin;

import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;

@BuiltinClass("Object")
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
	@BuiltinMethod(name = "isCallable")
	protected void isCallable(VM vm) {
		vm.returnFromVM(BoolObj.valueOf(isCallable()));
	}
	public int arity() {
		return 0;
	}
	public void onCall(VM vm) {
		new TypeError("The object is not callable.").throwSelfNative();
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
	@BuiltinMethod(name = "freeze")
	protected void freeze(VM vm) {
		freeze();
		vm.returnNilFromVM();
	}

	
	public boolean frozen() {
		return frozen;
	}
	@BuiltinMethod(name = "frozen")
	private void frozen(VM vm) {
		vm.returnFromVM(BoolObj.valueOf(frozen()));
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
	@BuiltinMethod(name = Names.METHOD_SET_ATTR, argc = 2)
	private void setAttr(VM vm) {
		String attr = ObjectHelper.asString(vm.pop());
		checkIsFrozen();
		CandyObject value = vm.pop();
		vm.returnFromVM(setAttr(vm, attr, value));
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
	
	@BuiltinMethod(name = Names.METHOD_GET_ATTR, argc = 1)
	private void getAttr(VM vm) {
		String attr = ObjectHelper.asString(vm.pop());
		CandyObject ret = getAttr(vm, attr);
		if (ret != null) {
			vm.returnFromVM(ret);
			return;
		}
		getUnknownAttrApi(vm, attr);
	}
	
	public abstract void getUnknownAttrApi(VM vm, String attr);
	public abstract CandyObject getUnknownAttrApiExeUser(VM vm, String attr);
	public CandyObject getUnknownAttr(VM vm, String attr) {
		AttributeError.checkAttributeNull(this, attr, null);
		throw new Error();
	}
	@BuiltinMethod(name = Names.METHOD_GET_UNKNOWN_ATTR, argc = 1)
	private void getUnknownAttr(VM vm) {
		vm.returnFromVM(getUnknownAttr(vm, ObjectHelper.asString(vm.pop())));
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
	@BuiltinMethod(name = Names.METHOD_SET_ITEM, argc = 2)
	private void setItem(VM vm) {
		checkIsFrozen();
		CandyObject key = vm.pop();
		CandyObject value = vm.pop();
		setItem(vm, key, value);
		vm.returnFromVM(value);
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
	@BuiltinMethod(name = Names.METHOD_GET_ITEM, argc = 1)
	private void getItem(VM vm) {
		vm.returnFromVM(getItem(vm, vm.pop()));
	}

	
	public abstract void equalsApi(VM vm, CandyObject operand);
	public abstract BoolObj equalsApiExeUser(VM vm, CandyObject operand);
	public BoolObj equals(VM vm, CandyObject operand) {
		return BoolObj.valueOf(this == operand);
	}
	@BuiltinMethod(name = Names.METHOD_EQUALS, argc = 1)
	private void equals(VM vm) {
		vm.returnFromVM(equals(vm, vm.pop()));
	}

	
	public abstract void hashCodeApi(VM vm);
	public abstract IntegerObj hashCodeApiExeUser(VM vm);
	public IntegerObj hashCode(VM vm) {
		return IntegerObj.valueOf(super.hashCode());
	}
	@BuiltinMethod(name = Names.METHOD_HASH_CODE)
	private void hashCodeMethod(VM vm) {
		vm.returnFromVM(hashCode(vm));
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
	@BuiltinMethod(name = Names.METHOD_STR_VALUE)
	protected void stringValue(VM vm) {
		vm.returnFromVM(str(vm));
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
	@BuiltinMethod(name = Names.METHOD_ITERATOR)
	private void iteratorMethod(VM vm) {
		vm.returnFromVM(iterator(vm));
	}
	
	
	@BuiltinMethod() 
	private void objDefaultInitializer(VM vm) { vm.returnFromVM(this); }

	@BuiltinMethod(name = "_class")
	protected void getClass(VM vm) {
		vm.returnFromVM(getCandyClass());
	}


	public abstract void positiveApi(VM vm);
	public abstract CandyObject positiveApiExeUser(VM vm);
	public CandyObject positive(VM vm) {
		new TypeError("+").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_POSITIVE, argc = 0)
	private void positiveMethod(VM vm) {
		vm.returnFromVM(positive(vm));
	}

	public abstract void negativeApi(VM vm);
	public abstract CandyObject negativeApiExeUser(VM vm);
	public CandyObject negative(VM vm) {
		new TypeError("-").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_NEGATIVE, argc = 0)
	private void negativeMethod(VM vm) {
		vm.returnFromVM(negative(vm));
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
	@BuiltinMethod(name = Names.METHOD_OP_ADD, argc = 1)
	private void addMethod(VM vm) {
		vm.returnFromVM(add(vm, vm.pop()));
	}

	public abstract void subApi(VM vm, CandyObject operand);
	public abstract CandyObject subApiExeUser(VM vm, CandyObject operand);
	public CandyObject sub(VM vm, CandyObject operand) {
		new TypeError("-").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_SUB, argc = 1)
	private void subMethod(VM vm) {
		vm.returnFromVM(sub(vm, vm.pop()));
	}

	public abstract void mulApi(VM vm, CandyObject operand);
	public abstract CandyObject mulApiExeUser(VM vm, CandyObject operand);
	public CandyObject mul(VM vm, CandyObject operand) {
		new TypeError("*").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_MUL, argc = 1)
	private void mulMethod(VM vm) {
		vm.returnFromVM(mul(vm, vm.pop()));
	}

	public abstract void divApi(VM vm, CandyObject operand);
	public abstract CandyObject divApiExeUser(VM vm, CandyObject operand);
	public CandyObject div(VM vm, CandyObject operand) {
		new TypeError("/").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_DIV, argc = 1)
	private void divMethod(VM vm) {
		vm.returnFromVM(div(vm, vm.pop()));
	}

	public abstract void modApi(VM vm, CandyObject operand);
	public abstract CandyObject modApiExeUser(VM vm, CandyObject operand);
	public CandyObject mod(VM vm, CandyObject operand) {
		new TypeError("%").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_MOD, argc = 1)
	private void modMethod(VM vm) {
		vm.returnFromVM(mod(vm, vm.pop()));
	}

	public abstract void gtApi(VM vm, CandyObject operand);
	public abstract BoolObj gtApiExeUser(VM vm, CandyObject operand);
	public BoolObj gt(VM vm, CandyObject operand) {
		new TypeError(">").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_GT, argc = 1)
	private void gtMethod(VM vm) {
		vm.returnFromVM(gt(vm, vm.pop()));
	}

	public abstract void gteqApi(VM vm, CandyObject operand);
	public abstract BoolObj gteqApiExeUser(VM vm, CandyObject operand);
	public BoolObj gteq(VM vm, CandyObject operand) {
		new TypeError(">=").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_GTEQ, argc = 1)
	private void gteqMethod(VM vm) {
		vm.returnFromVM(gteq(vm, vm.pop()));
	}

	public abstract void ltApi(VM vm, CandyObject operand);
	public abstract BoolObj ltApiExeUser(VM vm, CandyObject operand);
	public BoolObj lt(VM vm, CandyObject operand) {
		new TypeError("<").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_LT, argc = 1)
	private void ltMethod(VM vm) {
		vm.returnFromVM(lt(vm, vm.pop()));
	}

	public abstract void lteqApi(VM vm, CandyObject operand);
	public abstract BoolObj lteqApiExeUser(VM vm, CandyObject operand);
	public BoolObj lteq(VM vm, CandyObject operand) {
		new TypeError("<=").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_LTEQ, argc = 1)
	private void lteqMethod(VM vm) {
		vm.returnFromVM(lteq(vm, vm.pop()));
	}

}
