package com.nano.candy.interpreter.i2.builtin;

import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.MethodObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import java.util.HashMap;

public class CandyObjEntity extends CandyObject {

	private HashMap<String, CandyObject> attrs;
	
	/**
	 * Cache.
	 */
	private CallableObj attrGetter;
	private CallableObj attrSetter;
	private CallableObj unknownAttrGetter;
	private CallableObj itemGetter;
	private CallableObj itemSetter;
	private CallableObj equalTo;
	private CallableObj hashcode;
	private CallableObj stringVal;
	
	public CandyObjEntity(CandyClass clazz) {
		super(clazz);
		attrs = new HashMap<>();
	}
	
	protected static CandyObject invokeMethod(VM vm, CallableObj method) {
		return invokeMethod(vm, method, null);
	}
	
	protected static CandyObject invokeMethod(VM vm, CallableObj method, CandyClass expectedRetType) {
		CandyObject retObj = ObjectHelper.callFunction(vm, method);
		if (expectedRetType != null) {
			TypeError.checkTypeMatched(
				expectedRetType, retObj,
				"'%s' must return '%s' (actual type %s).",
				method.declredName(),
				expectedRetType.getClassName(), retObj.getCandyClassName()
			);
		}
		return retObj;
	}
	
	protected CallableObj getBoundMethod(String name, int expectedArity) {
		return getBoundMethod(name, expectedArity, null);
	}
	
	protected CallableObj getBoundMethod(String name, int expectedArity, CallableObj cache) {
		if (cache != null) {
			return cache;
		}
		cache = getCandyClass().getBoundMethod(name, this);
		if (cache != null) {
			ArgumentError.checkArity(cache, expectedArity);
			return cache;
		}
		return null;
	}
	
	public CandyObject invokeApiExeUser(VM vm, String name, CandyObject... args) {
		CallableObj callable = getCandyClass().getBoundMethod(name, this);
		AttributeError.checkAttributeNull(this, name, callable);
		return ObjectHelper.callFunction(vm, callable, args);
	}
	
	@Override
	public final void setAttrApi(VM vm, String attr, CandyObject value) {
		checkIsFrozen();
		attrSetter = getBoundMethod(Names.METHOD_SET_ATTR, 2, attrSetter);
		vm.push(value);
		vm.push(StringObj.valueOf(attr));
		attrSetter.onCall(vm);
	}

	@Override
	public final CandyObject setAttrApiExeUser(VM vm, String attr, CandyObject value) {
		checkIsFrozen();
		attrSetter = getBoundMethod(Names.METHOD_SET_ATTR, 2, attrSetter);
		vm.push(value);
		vm.push(StringObj.valueOf(attr));	
		return invokeMethod(vm, attrGetter);
	}
	
	@Override
	public CandyObject setAttr(VM vm, String attr, CandyObject ref) {
		attrs.put(attr, ref);
		return ref;
	}
	
	/**
	 * @see #getAttr(VM, String)
	 * @see CandyObject#getAttr(VM)
	 */
	@Override
	public final void getAttrApi(VM vm, String attr) {
		attrGetter = getBoundMethod(Names.METHOD_GET_ATTR, 1, attrGetter);
		vm.push(StringObj.valueOf(attr));
		attrGetter.onCall(vm);
	}
	
	@Override
	public final CandyObject getAttrApiExeUser(VM vm, String attr) {
		attrGetter = getBoundMethod(Names.METHOD_GET_ATTR, 1, attrGetter);
		vm.push(StringObj.valueOf(attr));
		return invokeMethod(vm, attrGetter);
	}
	
	@Override
	public CandyObject getAttr(VM vm, String attr) {
		CandyObject value = attrs.get(attr);
		if (value == null) {
			MethodObj method = getCandyClass().getBoundMethod(attr, this);
			if (method == null) {
				return null;
			}
			// cache
			attrs.put(attr, method);
			return method;
		}
		return value;
	}

	@Override
	public final CandyObject getUnknownAttrApiExeUser(VM vm, String attr) {
		unknownAttrGetter = getBoundMethod(
			Names.METHOD_GET_UNKNOWN_ATTR, 1, unknownAttrGetter);
		vm.push(StringObj.valueOf(attr));
		return invokeMethod(vm, unknownAttrGetter);
	}

	@Override
	public final void getUnknownAttrApi(VM vm, String attr) {
		unknownAttrGetter = getBoundMethod(
			Names.METHOD_GET_UNKNOWN_ATTR, 1, unknownAttrGetter);
		vm.push(StringObj.valueOf(attr));
		unknownAttrGetter.onCall(vm);
	}

	@Override
	public final void setItemApi(VM vm, CandyObject key, CandyObject value) {
		checkIsFrozen();
		itemSetter = getBoundMethod(Names.METHOD_SET_ITEM, 2, itemSetter);
		vm.push(value);
		vm.push(key);
		itemSetter.onCall(vm);
	}

	@Override
	public final CandyObject setItemApiExeUser(VM vm, CandyObject key, CandyObject value) {
		checkIsFrozen();
		itemSetter = getBoundMethod(Names.METHOD_SET_ITEM, 2, itemSetter);
		vm.push(value);
		vm.push(key);
		return invokeMethod(vm, itemSetter);
	}

	@Override
	public final void getItemApi(VM vm, CandyObject key) {
		itemGetter = getBoundMethod(Names.METHOD_GET_ITEM, 1, itemGetter);
		vm.push(key);
		itemGetter.onCall(vm);
	}

	@Override
	public final CandyObject getItemApiExeUser(VM vm, CandyObject key) {
		itemGetter = getBoundMethod(Names.METHOD_GET_ITEM, 1, itemGetter);
		vm.push(key);
		return invokeMethod(vm, itemGetter);
	}

	@Override
	public final void equalsApi(VM vm, CandyObject operand) {
		equalTo = getBoundMethod(Names.METHOD_EQUALS, 1, equalTo);
		vm.push(operand);
		equalTo.onCall(vm);
	}

	@Override
	public final BoolObj equalsApiExeUser(VM vm, CandyObject operand) {
		equalTo = getBoundMethod(Names.METHOD_EQUALS, 1, equalTo);
		vm.push(operand);
		return (BoolObj) 
			invokeMethod(vm, equalTo, BoolObj.BOOL_CLASS);
	}

	@Override
	public final void hashCodeApi(VM vm) {
		hashcode = getBoundMethod(Names.METHOD_HASH_CODE, 0, hashcode);
		hashcode.onCall(vm);
	}

	@Override
	public final IntegerObj hashCodeApiExeUser(VM vm) {
		hashcode = getBoundMethod(Names.METHOD_HASH_CODE, 0, hashcode);
		if (!hashcode.isBuiltin()) {
			return (IntegerObj) 
				invokeMethod(vm, hashcode, IntegerObj.INTEGER_CLASS);
		}
		return hashCode(vm);
	}
	
	@Override
	public final StringObj strApiExeUser(VM vm) {
		stringVal = getBoundMethod(Names.METHOD_STR_VALUE, 0, stringVal);
		return (StringObj) 
			invokeMethod(vm, stringVal, StringObj.STRING_CLASS);
	}

	@Override
	public final CandyObject iteratorApiExeUser(VM vm) {
		CallableObj iterator = getBoundMethod(Names.METHOD_ITERATOR, 0, null);
		return invokeMethod(vm, iterator);
	}
	
	// Operator Methods
	
	
	private void binaryApi(VM vm, String name, CandyObject operand) {
		CallableObj boundMet = getBoundMethod(name, 1, null);
		vm.push(operand);
		boundMet.onCall(vm);
	}
	
	private CandyObject binaryApiExeUser(VM vm, String name, CandyObject operand, 
	                                     CandyClass expextedReturnType) {
		CallableObj boundMet = getBoundMethod(name, 1, null);
		vm.push(operand);
		return invokeMethod(vm, boundMet, expextedReturnType);
	}
	
	private void unaryApi(VM vm, String name) {
		CallableObj boundMet = getBoundMethod(name, 0, null);
		boundMet.onCall(vm);
	}

	private CandyObject unaryApiExeUser(VM vm, String name) {
		CallableObj boundMet = getBoundMethod(name, 0, null);
		return invokeMethod(vm, boundMet);
	}
	
	@Override
	public final void positiveApi(VM vm) {
		unaryApi(vm, Names.METHOD_OP_POSITIVE);
	}

	@Override
	public final CandyObject positiveApiExeUser(VM vm) {
		return unaryApiExeUser(vm, Names.METHOD_OP_POSITIVE);
	}

	@Override
	public final void negativeApi(VM vm) {
		unaryApi(vm, Names.METHOD_OP_NEGATIVE);
	}

	@Override
	public final CandyObject negativeApiExeUser(VM vm) {
		return unaryApiExeUser(vm, Names.METHOD_OP_NEGATIVE);
	}

	@Override
	public final void addApi(VM vm, CandyObject operand) {
		binaryApi(vm, Names.METHOD_OP_ADD, operand);
	}

	@Override
	public final CandyObject addApiExeUser(VM vm, CandyObject operand) {
		return binaryApiExeUser(vm, Names.METHOD_OP_ADD, operand, null);
	}

	@Override
	public final void subApi(VM vm, CandyObject operand) {
		binaryApi(vm, Names.METHOD_OP_SUB, operand);
	}

	@Override
	public final CandyObject subApiExeUser(VM vm, CandyObject operand) {
		return binaryApiExeUser(vm, Names.METHOD_OP_SUB, operand, null);
	}

	@Override
	public final void mulApi(VM vm, CandyObject operand) {
		binaryApi(vm, Names.METHOD_OP_MUL, operand);
	}

	@Override
	public final CandyObject mulApiExeUser(VM vm, CandyObject operand) {
		return binaryApiExeUser(vm, Names.METHOD_OP_MUL, operand, null);
	}

	@Override
	public final void divApi(VM vm, CandyObject operand) {
		binaryApi(vm, Names.METHOD_OP_DIV, operand);
	}

	@Override
	public final CandyObject divApiExeUser(VM vm, CandyObject operand) {
		return binaryApiExeUser(vm, Names.METHOD_OP_DIV, operand, null);
	}

	@Override
	public final void modApi(VM vm, CandyObject operand) {
		binaryApi(vm, Names.METHOD_OP_MOD, operand);
	}

	@Override
	public final CandyObject modApiExeUser(VM vm, CandyObject operand) {
		return binaryApiExeUser(vm, Names.METHOD_OP_MOD, operand, null);
	}

	
	@Override
	public final void gtApi(VM vm, CandyObject operand) {
		binaryApi(vm, Names.METHOD_OP_GT, operand);
	}

	@Override
	public final BoolObj gtApiExeUser(VM vm, CandyObject operand) {
		return (BoolObj) binaryApiExeUser(
			vm, Names.METHOD_OP_GT, operand, BoolObj.BOOL_CLASS);
	}

	@Override
	public final void gteqApi(VM vm, CandyObject operand) {
		binaryApi(vm, Names.METHOD_OP_GTEQ, operand);
	}

	@Override
	public final BoolObj gteqApiExeUser(VM vm, CandyObject operand) {
		return (BoolObj) binaryApiExeUser(
			vm, Names.METHOD_OP_GTEQ, operand, BoolObj.BOOL_CLASS);
	}

	@Override
	public final void ltApi(VM vm, CandyObject operand) {
		binaryApi(vm, Names.METHOD_OP_LT, operand);
	}

	@Override
	public final BoolObj ltApiExeUser(VM vm, CandyObject operand) {
		return (BoolObj) binaryApiExeUser(
			vm, Names.METHOD_OP_LT, operand, BoolObj.BOOL_CLASS);
	}

	@Override
	public final void lteqApi(VM vm, CandyObject operand) {
		binaryApi(vm, Names.METHOD_OP_LTEQ, operand);
	}

	@Override
	public final BoolObj lteqApiExeUser(VM vm, CandyObject operand) {
		return (BoolObj) binaryApiExeUser(
			vm, Names.METHOD_OP_LTEQ, operand, BoolObj.BOOL_CLASS);
	}
	
}
