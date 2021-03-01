package com.nano.candy.interpreter.i2.builtin;

import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.MethodObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.error.ArgumentError;
import com.nano.candy.interpreter.i2.error.TypeError;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import java.util.HashMap;

public final class CandyObjEntity extends CandyObject {

	protected HashMap<String, CandyObject> attrs;
	
	private CallableObj attrGetter;
	private CallableObj attrSetter;
	private CallableObj itemGetter;
	private CallableObj itemSetter;
	private CallableObj equalTo;
	private CallableObj hashcode;
	private CallableObj stringVal;
	
	public CandyObjEntity(CandyClass clazz) {
		super(clazz);
		attrs = new HashMap<>();
	}
	
	private CallableObj getBoundMethod(String name, int arity, CallableObj cache) {
		if (cache != null) {
			return cache;
		}
		cache = getCandyClass().getBoundMethod(name, this);
		if (cache != null) {
			ArgumentError.checkArity(cache, arity);
			return cache;
		}
		return null;
	}
	
	private CandyObject invokeUserMethod(VM vm, CallableObj userMethod, CandyClass expectedRetType) {
		CandyObject retObj = ObjectHelper.callUserFunction(vm, userMethod);
		TypeError.checkTypeMatched(expectedRetType, retObj,
			"'%s' must be return '%s' type.", 
			userMethod.declredName(), expectedRetType.getClassName());
		return retObj;
	}
	
	@Override
	public void setAttrApi(VM vm, String attr, CandyObject value) {
		checkIsFrozen(attr);
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
	public CandyObject setAttr(VM vm, String attr, CandyObject ref) {
		attrs.put(attr, ref);
		return ref;
	}

	@Override
	public void getAttrApi(VM vm, String attr) {
		CandyObject obj = getAttr(vm, attr);
		if (obj == null) {
			attrGetter = getBoundMethod(Names.METHOD_GET_ATTR, 1, attrGetter);
			if (!attrGetter.isBuiltin()) {
				vm.push(StringObj.valueOf(attr));
				attrGetter.onCall(vm);
				return;
			}
			// error
			vm.returnFromVM(getUnknownAttr(vm, attr));
			return;
		}
		vm.returnFromVM(obj);
	}

	@Override
	public CandyObject getAttrApiExeUser(VM vm, String attr) {
		CandyObject obj = getAttr(vm, attr);
		if (obj == null) {
			attrGetter = getBoundMethod(Names.METHOD_GET_ATTR, 1, attrGetter);
			if (!attrGetter.isBuiltin()) {
				vm.push(StringObj.valueOf(attr));			
				return ObjectHelper.callUserFunction(vm, attrGetter);
			}
			// error
			return getUnknownAttr(vm, attr);
		}
		return obj;
	}
	
	@Override
	public CandyObject getAttr(VM vm, String attr) {
		CandyObject value = attrs.get(attr);
		if (value == null) {
			MethodObj method = getCandyClass().getBoundMethod(attr, this);
			if (method == null) {
				return null;
			}
			attrs.put(attr, method);
			return method;
		}
		return value;
	}
	
	@Override
	public void setItemApi(VM vm) {
		itemSetter = getBoundMethod(Names.METHOD_SET_ITEM, 2, itemSetter);
		if (!itemSetter.isBuiltin()) {
			itemSetter.onCall(vm);
		} else {
			super.setItem(vm);
		}
	}

	@Override
	public void getItemApi(VM vm) {
		itemGetter = getBoundMethod(Names.METHOD_GET_ITEM, 1, itemGetter);
		if (!itemGetter.isBuiltin()){
			itemGetter.onCall(vm);
		} else {
			super.getItem(vm);
		}
	}

	@Override
	public void equalsApi(VM vm, CandyObject operand) {
		equalTo = getBoundMethod(Names.METHOD_EQUALS, 1, null);
		if (!equalTo.isBuiltin()) {
			vm.push(operand);
			equalTo.onCall(vm);
		} else {
			vm.returnFromVM(equals(vm, operand));
		}
	}

	@Override
	public BoolObj equalsApiExeUser(VM vm, CandyObject operand) {
		equalTo = getBoundMethod(Names.METHOD_EQUALS, 1, equalTo);
		if (!equalTo.isBuiltin()) {
			vm.push(operand);
			return (BoolObj) 
				invokeUserMethod(vm, equalTo, BoolObj.BOOL_CLASS);
		} 
		return equals(vm, operand);
	}

	@Override
	public void hashCodeApi(VM vm) {
		hashcode = getBoundMethod(Names.METHOD_HASH_CODE, 1, hashcode);
		if (!hashcode.isBuiltin()) {
			hashcode.onCall(vm);
			return;
		}
		vm.returnFromVM(hashCode(vm));
	}

	@Override
	public IntegerObj hashCodeApiExeUser(VM vm) {
		hashcode = getBoundMethod(Names.METHOD_HASH_CODE, 1, hashcode);
		if (!hashcode.isBuiltin()) {
			return (IntegerObj) 
				invokeUserMethod(vm, hashcode, IntegerObj.INTEGER_CLASS);
		}
		return hashCode(vm);
	}

	@Override
	public StringObj strApiExeUser(VM vm) {
		stringVal = getBoundMethod(Names.METHOD_STR_VALUE, 0, stringVal);
		if (!stringVal.isBuiltin()) {
			return (StringObj) 
				invokeUserMethod(vm, stringVal, StringObj.STRING_CLASS);
		} 
		return str(vm);
	}
	
	
	
	// Operator Methods
	
	
	private boolean binaryApi(VM vm, String name, CandyObject operand) {
		CallableObj callable = getCandyClass().getMethod(name);
		if (!callable.isBuiltin()) {
			MethodObj boundMet = new MethodObj(this, callable);
			vm.push(operand);
			boundMet.onCall(vm);
			return true;
		}
		return false;
	}
	
	private CandyObject binaryApiExeUser(VM vm, String name, CandyObject operand, CandyClass clazz) {
		CallableObj callable = getCandyClass().getMethod(name);
		if (!callable.isBuiltin()) {
			MethodObj boundMet = new MethodObj(this, callable);
			vm.push(operand);
			if (clazz == null) {
				return ObjectHelper.callUserFunction(vm, boundMet);
			}
			return invokeUserMethod(vm, boundMet, clazz);
		}
		return null;
	}
	
	private boolean unaryApi(VM vm, String name) {
		CallableObj callable = getCandyClass().getMethod(name);
		if (!callable.isBuiltin()) {
			MethodObj boundMet = new MethodObj(this, callable);
			boundMet.onCall(vm);
			return true;
		}
		return false;
	}

	private CandyObject unaryApiExeUser(VM vm, String name) {
		CallableObj callable = getCandyClass().getMethod(name);
		if (!callable.isBuiltin()) {
			MethodObj boundMet = new MethodObj(this, callable);
			return ObjectHelper.callUserFunction(vm, boundMet);
		}
		return null;
	}
	
	@Override
	public void positiveApi(VM vm) {
		if (!unaryApi(vm, Names.METHOD_OP_POSITIVE)) {
			vm.returnFromVM(positive(vm));
		}
	}

	@Override
	public CandyObject positiveApiExeUser(VM vm) {
		CandyObject obj = unaryApiExeUser(vm, Names.METHOD_OP_POSITIVE);
		if (obj == null) {
			return positive(vm);
		}
		return obj;
	}

	@Override
	public void negativeApi(VM vm) {
		if (!unaryApi(vm, Names.METHOD_OP_NEGATIVE)) {
			vm.returnFromVM(negative(vm));
		}
	}

	@Override
	public CandyObject negativeApiExeUser(VM vm) {
		CandyObject obj = unaryApiExeUser(vm, Names.METHOD_OP_NEGATIVE);
		if (obj == null) {
			return negative(vm);
		}
		return obj;
	}

	@Override
	public void addApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_ADD, operand)) {
			vm.returnFromVM(add(vm, operand));
		}
	}

	@Override
	public CandyObject addApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_ADD, operand, null);
		if (obj == null) {
			return add(vm, operand);
		}
		return obj;
	}

	@Override
	public void subApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_SUB, operand)) {
			vm.returnFromVM(sub(vm, operand));
		}
	}

	@Override
	public CandyObject subApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_SUB, operand, null);
		if (obj == null) {
			return sub(vm, operand);
		}
		return obj;
	}

	@Override
	public void mulApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_MUL, operand)) {
			vm.returnFromVM(mul(vm, operand));
		}
	}

	@Override
	public CandyObject mulApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_MUL, operand, null);
		if (obj == null) {
			return mul(vm, operand);
		}
		return obj;
	}

	@Override
	public void divApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_DIV, operand)) {
			vm.returnFromVM(div(vm, operand));
		}
	}

	@Override
	public CandyObject divApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_DIV, operand, null);
		if (obj == null) {
			return div(vm, operand);
		}
		return obj;
	}

	@Override
	public void modApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_MOD, operand)) {
			vm.returnFromVM(mod(vm, operand));
		}
	}

	@Override
	public CandyObject modApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_MOD, operand, null);
		if (obj == null) {
			return mod(vm, operand);
		}
		return obj;
	}

	
	@Override
	public void gtApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_GT, operand)) {
			vm.returnFromVM(gt(vm, operand));
		}
	}

	@Override
	public BoolObj gtApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_GT, operand, BoolObj.BOOL_CLASS);
		if (obj == null) {
			return gt(vm, operand);
		}
		return (BoolObj) obj;
	}

	@Override
	public void gteqApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_GTEQ, operand)) {
			vm.returnFromVM(gteq(vm, operand));
		}
	}

	@Override
	public BoolObj gteqApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_GTEQ, operand, BoolObj.BOOL_CLASS);
		if (obj == null) {
			return gteq(vm, operand);
		}
		return (BoolObj) obj;
	}

	@Override
	public void ltApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_LT, operand)) {
			vm.returnFromVM(lt(vm, operand));
		}
	}

	@Override
	public BoolObj ltApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_LT, operand, BoolObj.BOOL_CLASS);
		if (obj == null) {
			return lt(vm, operand);
		}
		return (BoolObj) obj;
	}

	@Override
	public void lteqApi(VM vm, CandyObject operand) {
		if (!binaryApi(vm, Names.METHOD_OP_LTEQ, operand)) {
			vm.returnFromVM(lteq(vm, operand));
		}
	}

	@Override
	public BoolObj lteqApiExeUser(VM vm, CandyObject operand) {
		CandyObject obj = binaryApiExeUser(vm, Names.METHOD_OP_LTEQ, operand, BoolObj.BOOL_CLASS);
		if (obj == null) {
			return lteq(vm, operand);
		}
		return (BoolObj) obj;
	}
	
}
