package com.nano.candy.interpreter.i2.builtin;

import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.MethodObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
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
	
	@Override
	public final void setAttrApi(VM vm, String attr, CandyObject value) {
		checkIsFrozen();
		attrSetter = getBoundMethod(Names.METHOD_SET_ATTR, 2, attrSetter);
		if (!attrSetter.isBuiltin()) {		
			vm.push(value);
			vm.push(StringObj.valueOf(attr));
			attrSetter.onCall(vm);
		} else {
			vm.returnFromVM(setAttr(vm, attr, value));
		}
	}

	@Override
	public final CandyObject setAttrApiExeUser(VM vm, String attr, CandyObject value) {
		checkIsFrozen();
		attrSetter = getBoundMethod(Names.METHOD_SET_ATTR, 2, attrSetter);
		if (!attrSetter.isBuiltin()) {		
			vm.push(value);
			vm.push(StringObj.valueOf(attr));	
			return invokeMethod(vm, attrGetter);
		} 
		return setAttr(vm, attr, value);
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
		if (!unknownAttrGetter.isBuiltin()) {
			vm.push(StringObj.valueOf(attr));
			return invokeMethod(vm, unknownAttrGetter);
		} else {
			return getUnknownAttr(vm, attr);
		}
	}

	@Override
	public final void getUnknownAttrApi(VM vm, String attr) {
		unknownAttrGetter = getBoundMethod(
			Names.METHOD_GET_UNKNOWN_ATTR, 1, unknownAttrGetter);
		if (!unknownAttrGetter.isBuiltin()) {
			vm.push(StringObj.valueOf(attr));
			unknownAttrGetter.onCall(vm);
		} else {
			vm.returnFromVM(getUnknownAttr(vm, attr));
		}
	}

	@Override
	public final void setItemApi(VM vm, CandyObject key, CandyObject value) {
		checkIsFrozen();
		itemSetter = getBoundMethod(Names.METHOD_SET_ITEM, 2, itemSetter);
		if (!itemSetter.isBuiltin()) {
			vm.push(value);
			vm.push(key);
			itemSetter.onCall(vm);
		} else {
			vm.returnFromVM(setItem(vm, key, value));
		}
	}

	@Override
	public final CandyObject setItemApiExeUser(VM vm, CandyObject key, CandyObject value) {
		checkIsFrozen();
		itemSetter = getBoundMethod(Names.METHOD_SET_ITEM, 2, itemSetter);
		if (!itemSetter.isBuiltin()) {
			vm.push(value);
			vm.push(key);
			return invokeMethod(vm, itemSetter);
		} else {
			 return setItem(vm, key, value);
		}
	}

	@Override
	public final void getItemApi(VM vm, CandyObject key) {
		itemGetter = getBoundMethod(Names.METHOD_GET_ITEM, 1, itemGetter);
		if (!itemGetter.isBuiltin()) {
			vm.push(key);
			itemGetter.onCall(vm);
		} else {
			vm.returnFromVM(getItem(vm, key));
		}
	}

	@Override
	public final CandyObject getItemApiExeUser(VM vm, CandyObject key) {
		itemGetter = getBoundMethod(Names.METHOD_GET_ITEM, 1, itemGetter);
		if (!itemGetter.isBuiltin()) {
			vm.push(key);
			return invokeMethod(vm, itemGetter);
		} else {
			return getItem(vm, key);
		}
	}

	@Override
	public final void equalsApi(VM vm, CandyObject operand) {
		equalTo = getBoundMethod(Names.METHOD_EQUALS, 1, equalTo);
		if (!equalTo.isBuiltin()) {
			vm.push(operand);
			equalTo.onCall(vm);
		} else {
			vm.returnFromVM(equals(vm, operand));
		}
	}

	@Override
	public final BoolObj equalsApiExeUser(VM vm, CandyObject operand) {
		equalTo = getBoundMethod(Names.METHOD_EQUALS, 1, equalTo);
		if (!equalTo.isBuiltin()) {
			vm.push(operand);
			return (BoolObj) 
				invokeMethod(vm, equalTo, BoolObj.BOOL_CLASS);
		} 
		return equals(vm, operand);
	}

	@Override
	public final void hashCodeApi(VM vm) {
		hashcode = getBoundMethod(Names.METHOD_HASH_CODE, 0, hashcode);
		if (!hashcode.isBuiltin()) {
			hashcode.onCall(vm);
			return;
		}
		vm.returnFromVM(hashCode(vm));
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
		if (!stringVal.isBuiltin()) {
			return (StringObj) 
				invokeMethod(vm, stringVal, StringObj.STRING_CLASS);
		} 
		return str(vm);
	}

	@Override
	public final CandyObject iteratorApiExeUser(VM vm) {
		CallableObj iterator = getBoundMethod(Names.METHOD_ITERATOR, 0, null);
		if (!iterator.isBuiltin()) {
			return invokeMethod(vm, iterator);
		}
		return iterator(vm);
	}
	
	// Operator Methods
	
	
	private boolean binaryApi(VM vm, String name, CandyObject operand) {
		CallableObj callable = getCandyClass().getMethod(name);
		if (!callable.isBuiltin()) {
			MethodObj boundMet = new MethodObj(this, callable);
			ArgumentError.checkArity(boundMet, 1);
			vm.push(operand);
			boundMet.onCall(vm);
			return true;
		}
		return false;
	}
	
	private CandyObject binaryApiExeUser(VM vm, String name, CandyObject operand, 
	                                     CandyClass expextedReturnType) {
		CallableObj callable = getCandyClass().getMethod(name);
		if (!callable.isBuiltin()) {
			MethodObj boundMet = new MethodObj(this, callable);
			ArgumentError.checkArity(boundMet, 1);
			vm.push(operand);
			return invokeMethod(vm, boundMet, expextedReturnType);
		}
		return null;
	}
	
	private boolean unaryApi(VM vm, String name) {
		CallableObj callable = getCandyClass().getMethod(name);
		if (!callable.isBuiltin()) {
			MethodObj boundMet = new MethodObj(this, callable);
			ArgumentError.checkArity(boundMet, 0);
			boundMet.onCall(vm);
			return true;
		}
		return false;
	}

	private CandyObject unaryApiExeUser(VM vm, String name) {
		CallableObj callable = getCandyClass().getMethod(name);
		ArgumentError.checkArity(callable, 0);
		if (!callable.isBuiltin()) {
			MethodObj boundMet = new MethodObj(this, callable);
			ArgumentError.checkArity(boundMet, 0);
			return invokeMethod(vm, boundMet);
		}
		return null;
	}
	
	@Override
	public final void positiveApi(VM vm) {
		if (!unaryApi(vm, Names.METHOD_OP_POSITIVE)) {
			vm.returnFromVM(positive(vm));
		}
	}

	@Override
	public final CandyObject positiveApiExeUser(VM vm) {
		CandyObject obj = unaryApiExeUser(vm, Names.METHOD_OP_POSITIVE);
		if (obj == null) {
			return positive(vm);
		}
		return obj;
	}

	@Override
	public final void negativeApi(VM vm) {
		if (!unaryApi(vm, Names.METHOD_OP_NEGATIVE)) {
			vm.returnFromVM(negative(vm));
		}
	}

	@Override
	public final CandyObject negativeApiExeUser(VM vm) {
		CandyObject obj = unaryApiExeUser(vm, Names.METHOD_OP_NEGATIVE);
		if (obj == null) {
			return negative(vm);
		}
		return obj;
	}

	@Override
	public final void addApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_ADD, operand)) {
			vm.returnFromVM(add(vm, operand));
		}
	}

	@Override
	public final CandyObject addApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_ADD, operand, null);
		if (obj == null) {
			return add(vm, operand);
		}
		return obj;
	}

	@Override
	public final void subApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_SUB, operand)) {
			vm.returnFromVM(sub(vm, operand));
		}
	}

	@Override
	public final CandyObject subApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_SUB, operand, null);
		if (obj == null) {
			return sub(vm, operand);
		}
		return obj;
	}

	@Override
	public final void mulApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_MUL, operand)) {
			vm.returnFromVM(mul(vm, operand));
		}
	}

	@Override
	public final CandyObject mulApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_MUL, operand, null);
		if (obj == null) {
			return mul(vm, operand);
		}
		return obj;
	}

	@Override
	public final void divApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_DIV, operand)) {
			vm.returnFromVM(div(vm, operand));
		}
	}

	@Override
	public final CandyObject divApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_DIV, operand, null);
		if (obj == null) {
			return div(vm, operand);
		}
		return obj;
	}

	@Override
	public final void modApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_MOD, operand)) {
			vm.returnFromVM(mod(vm, operand));
		}
	}

	@Override
	public final CandyObject modApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_MOD, operand, null);
		if (obj == null) {
			return mod(vm, operand);
		}
		return obj;
	}

	
	@Override
	public final void gtApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_GT, operand)) {
			vm.returnFromVM(gt(vm, operand));
		}
	}

	@Override
	public final BoolObj gtApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_GT, operand, BoolObj.BOOL_CLASS);
		if (obj == null) {
			return gt(vm, operand);
		}
		return (BoolObj) obj;
	}

	@Override
	public final void gteqApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_GTEQ, operand)) {
			vm.returnFromVM(gteq(vm, operand));
		}
	}

	@Override
	public final BoolObj gteqApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_GTEQ, operand, BoolObj.BOOL_CLASS);
		if (obj == null) {
			return gteq(vm, operand);
		}
		return (BoolObj) obj;
	}

	@Override
	public final void ltApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_LT, operand)) {
			vm.returnFromVM(lt(vm, operand));
		}
	}

	@Override
	public final BoolObj ltApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_LT, operand, BoolObj.BOOL_CLASS);
		if (obj == null) {
			return lt(vm, operand);
		}
		return (BoolObj) obj;
	}

	@Override
	public final void lteqApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_LTEQ, operand)) {
			vm.returnFromVM(lteq(vm, operand));
		}
	}

	@Override
	public final BoolObj lteqApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_LTEQ, operand, BoolObj.BOOL_CLASS);
		if (obj == null) {
			return lteq(vm, operand);
		}
		return (BoolObj) obj;
	}
	
}
