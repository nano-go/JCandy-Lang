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

	public void freeze() {
		this.frozen = true;
	}

	public boolean frozen() {
		return frozen;
	}

	protected void throwFrozenObjError(String attr) {
		new AttributeError(
			"The frozen object can't be changed: %s.%s = someValue", 
			getCandyClass(), attr
		).throwSelfNative();
	}

	public final void checkIsFrozen(String attr) {
		if (frozen) {
			throwFrozenObjError(attr);
		}
	}
	
	public boolean isCallable() {
		return false;
	}
	
	public int arity() {
		return 0;
	}
	
	public void onCall(VM vm) {
		new TypeError("The object is not callable.").throwSelfNative();
	}
	
	public abstract void setAttrApi(VM vm, String attr, CandyObject value);
	public abstract CandyObject setAttr(VM vm, String attr, CandyObject value);
	
	public abstract void getAttrApi(VM vm, String attr);
	public abstract CandyObject getAttrApiExeUser(VM vm, String attr);
	
	public abstract CandyObject getAttr(VM vm, String attr);
	public CandyObject getUnknownAttr(VM vm, String attr) {
		AttributeError.checkAttributeNull(this, attr, null);
		throw new Error();
	}
	
	public abstract void setItemApi(VM vm);
	public CandyObject setItem(CandyObject key, CandyObject value) {
		new TypeError(
			"'%s'['%s'] = '%s'", 
			getCandyClassName(),
			key.getCandyClassName(),
			value.getCandyClassName()
		).throwSelfNative();
		return null;
	}
	
	public abstract void getItemApi(VM vm);
	public CandyObject getItem(CandyObject key) {
		new TypeError(
			"'%s'['%s']", getCandyClassName(), key.getCandyClassName()
		).throwSelfNative();
		return null;
	}

	public BoolObj not(VM vm) { 
		return boolValue(vm).not(vm); 
	}

	public abstract void equalsApi(VM vm, CandyObject operand);
	public abstract BoolObj equalsApiExeUser(VM vm, CandyObject operand);
	public BoolObj equals(VM vm, CandyObject operand) {
		return BoolObj.valueOf(this == operand);
	}
	
	public abstract void hashCodeApi(VM vm);
	public abstract IntegerObj hashCodeApiExeUser(VM vm);
	public IntegerObj hashCode(VM vm) {
		return IntegerObj.valueOf(super.hashCode());
	}

	public BoolObj boolValue(VM vm) {	
		return BoolObj.TRUE;
	}

	public abstract StringObj strApiExeUser(VM vm);
	public StringObj str(VM vm) {
		return StringObj.valueOf(this.toString());
	}

	public CandyObject iterator() {
		new TypeError("the object is not iterable.")
			.throwSelfNative();
		return null;
	}
	
	@Override
	public String toString() {
		return ObjectHelper.toString(
			getCandyClassName(), "at " + Integer.toHexString(hashCode())
		);
	}
	
	@BuiltinMethod() 
	public void objDefaultInitializer(VM vm) { vm.returnFromVM(this); }
	
	@BuiltinMethod(name = "_class")
	public void getClass(VM vm) {
		vm.returnFromVM(getCandyClass());
	}
	
	@BuiltinMethod(name = "isCallable")
	public void isCallable(VM vm) {
		vm.returnFromVM(BoolObj.valueOf(isCallable()));
	}
	
	@BuiltinMethod(name = Names.METHOD_HASH_CODE)
	public void hashCodeMethod(VM vm) {
		vm.returnFromVM(hashCode(vm));
	}
	
	@BuiltinMethod(name = Names.METHOD_GET_ATTR, argc = 1)
	public void getAttr(VM vm) {
		String attr = ObjectHelper.asString(vm.pop());
		CandyObject ret = getUnknownAttr(vm, attr);
		if (ret == null) {
			vm.returnNilFromVM();
			return;
		} 
		vm.returnFromVM(ret);
	}

	@BuiltinMethod(name = Names.METHOD_SET_ATTR, argc = 2)
	public void setAttr(VM vm) {
		String attr = ObjectHelper.asString(vm.pop());
		CandyObject value = vm.pop();
		vm.returnFromVM(setAttr(vm, attr, value));
	}
	
	@BuiltinMethod(name = Names.METHOD_GET_ITEM, argc = 1)
	public void getItem(VM vm) {
		vm.returnFromVM(getItem(vm.pop()));
	}
	
	@BuiltinMethod(name = Names.METHOD_SET_ITEM, argc = 2)
	public void setItem(VM vm) {
		CandyObject key = vm.pop();
		CandyObject value = vm.pop();
		setItem(key, value);
		vm.returnFromVM(value);
	}
	
	@BuiltinMethod(name = Names.METHOD_EQUALS, argc = 1)
	public void equals(VM vm){
		vm.returnFromVM(equals(vm, vm.pop()));
	}
	
	@BuiltinMethod(name = Names.METHOD_STR_VALUE)
	public void stringValue(VM vm) {
		vm.returnFromVM(str(vm));
	}
	
	@BuiltinMethod(name = "freeze")
	public void freeze(VM vm) {
		freeze();
		vm.returnNilFromVM();
	}
	
	@BuiltinMethod(name = "frozen")
	public void frozen(VM vm) {
		vm.returnFromVM(BoolObj.valueOf(frozen()));
	}
	
	@BuiltinMethod(name = Names.METHOD_ITERATOR)
	public void iterator(VM vm) {
		vm.returnFromVM(iterator());
	}
	
	
	// Operator Methods.
	//     Api: Call by VM.
	//     ApiExeUser: The method will be executed if it's override by user.
	
	public abstract void positiveApi(VM vm);
	public abstract CandyObject positiveApiExeUser(VM vm);
	public CandyObject positive(VM vm) {
		new TypeError("+").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_POSITIVE, argc = 0)
	public void positiveMethod(VM vm) {
		vm.returnFromVM(positive(vm));
	}

	public abstract void negativeApi(VM vm);
	public abstract CandyObject negativeApiExeUser(VM vm);
	public CandyObject negative(VM vm) {
		new TypeError("-").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_NEGATIVE, argc = 0)
	public void negativeMethod(VM vm) {
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
	public void addMethod(VM vm) {
		vm.returnFromVM(add(vm, vm.pop()));
	}

	public abstract void subApi(VM vm, CandyObject operand);
	public abstract CandyObject subApiExeUser(VM vm, CandyObject operand);
	public CandyObject sub(VM vm, CandyObject operand) {
		new TypeError("-").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_SUB, argc = 1)
	public void subMethod(VM vm) {
		vm.returnFromVM(sub(vm, vm.pop()));
	}

	public abstract void mulApi(VM vm, CandyObject operand);
	public abstract CandyObject mulApiExeUser(VM vm, CandyObject operand);
	public CandyObject mul(VM vm, CandyObject operand) {
		new TypeError("*").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_MUL, argc = 1)
	public void mulMethod(VM vm) {
		vm.returnFromVM(mul(vm, vm.pop()));
	}

	public abstract void divApi(VM vm, CandyObject operand);
	public abstract CandyObject divApiExeUser(VM vm, CandyObject operand);
	public CandyObject div(VM vm, CandyObject operand) {
		new TypeError("/").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_DIV, argc = 1)
	public void divMethod(VM vm) {
		vm.returnFromVM(div(vm, vm.pop()));
	}

	public abstract void modApi(VM vm, CandyObject operand);
	public abstract CandyObject modApiExeUser(VM vm, CandyObject operand);
	public CandyObject mod(VM vm, CandyObject operand) {
		new TypeError("%").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_MOD, argc = 1)
	public void modMethod(VM vm) {
		vm.returnFromVM(mod(vm, vm.pop()));
	}

	public abstract void gtApi(VM vm, CandyObject operand);
	public abstract BoolObj gtApiExeUser(VM vm, CandyObject operand);
	public BoolObj gt(VM vm, CandyObject operand) {
		new TypeError(">").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_GT, argc = 1)
	public void gtMethod(VM vm) {
		vm.returnFromVM(gt(vm, vm.pop()));
	}

	public abstract void gteqApi(VM vm, CandyObject operand);
	public abstract BoolObj gteqApiExeUser(VM vm, CandyObject operand);
	public BoolObj gteq(VM vm, CandyObject operand) {
		new TypeError(">=").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_GTEQ, argc = 1)
	public void gteqMethod(VM vm) {
		vm.returnFromVM(gteq(vm, vm.pop()));
	}

	public abstract void ltApi(VM vm, CandyObject operand);
	public abstract BoolObj ltApiExeUser(VM vm, CandyObject operand);
	public BoolObj lt(VM vm, CandyObject operand) {
		new TypeError("<").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_LT, argc = 1)
	public void ltMethod(VM vm) {
		vm.returnFromVM(lt(vm, vm.pop()));
	}

	public abstract void lteqApi(VM vm, CandyObject operand);
	public abstract BoolObj lteqApiExeUser(VM vm, CandyObject operand);
	public BoolObj lteq(VM vm, CandyObject operand) {
		new TypeError("<=").throwSelfNative();
		return null;
	}
	@BuiltinMethod(name = Names.METHOD_OP_LTEQ, argc = 1)
	public void lteqMethod(VM vm) {
		vm.returnFromVM(lteq(vm, vm.pop()));
	}
	
}
