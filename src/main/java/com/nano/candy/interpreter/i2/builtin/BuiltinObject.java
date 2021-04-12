package com.nano.candy.interpreter.i2.builtin;

import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.vm.VM;

public class BuiltinObject extends CandyObject {

	public BuiltinObject(CandyClass clazz) {
		super(clazz);
		freeze();
	}
	
	@Override
	public void positiveApi(VM vm) {
		vm.returnFromVM(positive(vm));
	}

	@Override
	public CandyObject positiveApiExeUser(VM vm) {
		return positive(vm);
	}

	@Override
	public void negativeApi(VM vm) {
		vm.returnFromVM(negative(vm));
	}

	@Override
	public CandyObject negativeApiExeUser(VM vm) {
		return negative(vm);
	}

	@Override
	public void addApi(VM vm, CandyObject operand) {
		vm.returnFromVM(add(vm, operand));
	}

	@Override
	public CandyObject addApiExeUser(VM vm, CandyObject operand) {
		return add(vm, operand);
	}

	@Override
	public void subApi(VM vm, CandyObject operand) {
		vm.returnFromVM(sub(vm, operand));
	}

	@Override
	public CandyObject subApiExeUser(VM vm, CandyObject operand) {
		return sub(vm, operand);
	}

	@Override
	public void mulApi(VM vm, CandyObject operand) {
		vm.returnFromVM(mul(vm, operand));
	}

	@Override
	public CandyObject mulApiExeUser(VM vm, CandyObject operand) {
		return mul(vm, operand);
	}

	@Override
	public void divApi(VM vm, CandyObject operand) {
		vm.returnFromVM(div(vm, operand));
	}

	@Override
	public CandyObject divApiExeUser(VM vm, CandyObject operand) {
		return div(vm, operand);
	}

	@Override
	public void modApi(VM vm, CandyObject operand) {
		vm.returnFromVM(mod(vm, operand));
	}

	@Override
	public CandyObject modApiExeUser(VM vm, CandyObject operand) {
		return mod(vm, operand);
	}

	@Override
	public void gtApi(VM vm, CandyObject operand) {
		vm.returnFromVM(gt(vm, operand));
	}

	@Override
	public BoolObj gtApiExeUser(VM vm, CandyObject operand) {
		return gt(vm, operand);
	}

	@Override
	public void gteqApi(VM vm, CandyObject operand) {
		vm.returnFromVM(gteq(vm, operand));
	}

	@Override
	public BoolObj gteqApiExeUser(VM vm, CandyObject operand) {
		return gteq(vm, operand);
	}

	@Override
	public void ltApi(VM vm, CandyObject operand) {
		vm.returnFromVM(lt(vm, operand));
	}

	@Override
	public BoolObj ltApiExeUser(VM vm, CandyObject operand) {
		return lt(vm, operand);
	}

	@Override
	public void lteqApi(VM vm, CandyObject operand) {
		vm.returnFromVM(lteq(vm, operand));
	}

	@Override
	public BoolObj lteqApiExeUser(VM vm, CandyObject operand) {
		return lteq(vm, operand);
	}
	
	@Override
	public void hashCodeApi(VM vm) {
		vm.returnFromVM(hashCodeApiExeUser(vm));
	}

	@Override
	public IntegerObj hashCodeApiExeUser(VM vm) {
		return hashCode(vm);
	}
	
	@Override
	public void setAttrApi(VM vm, String attr, CandyObject value) {
		vm.returnFromVM(setAttr(vm, attr, value));
	}
	
	@Override
	public CandyObject setAttr(VM vm, String attr, CandyObject ref) {
		throwFrozenObjError(attr);
		return null;
	}

	@Override
	public void getAttrApi(VM vm, String attr) {
		vm.returnFromVM(getAttrApiExeUser(vm, attr));
	}

	@Override
	public CandyObject getAttrApiExeUser(VM vm, String attr) {
		CandyObject value = getAttr(vm, attr);
		if (value == null) {
			getUnknownAttr(vm, attr);
			return null;
		}
		return value;
	}
	
	@Override
	public CandyObject getAttr(VM vm, String attr) {
		return getCandyClass().getBoundMethod(attr, this);
	}
	
	@Override
	public CandyObject getUnknownAttr(VM vm, String attr) {
		AttributeError.checkAttributeNull(this, attr, null);
		// Unreachable.
		throw new Error();
	}

	@Override
	public void equalsApi(VM vm, CandyObject operand) {
		vm.returnFromVM(equals(vm, operand));
	}

	@Override
	public BoolObj equalsApiExeUser(VM vm, CandyObject operand) {
		return equals(vm, operand);
	}
	
	@Override
	public StringObj strApiExeUser(VM vm) {
		return str(vm);
	}
	
	@Override
	public void setItemApi(VM vm) {
		setItem(vm);
	}

	@Override
	public void getItemApi(VM vm) {
		getItem(vm);
	}
}
