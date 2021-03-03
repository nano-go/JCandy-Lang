package com.nano.candy.interpreter.i2.builtin.functions;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.functions.BuiltinFunctions;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.Range;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.vm.VM;

public class BuiltinFunctions {
	
	public static final BuiltinFunctionEntity PRINT = 
		new BuiltinFunctionEntity("print", 1, BuiltinFunctions::print);
	public static void print(VM vm) {
		System.out.print(vm.pop().strApiExeUser(vm).value());
		vm.returnFromVM(NullPointer.nil());
	}
	
	public static final BuiltinFunctionEntity PRINTLN = 
		new BuiltinFunctionEntity("println", 1, BuiltinFunctions::println);
	public static void println(VM vm) {
		System.out.println(vm.pop().strApiExeUser(vm).value());
		vm.returnFromVM(NullPointer.nil());
	}
	
	public static final BuiltinFunctionEntity RANGE = 
		new BuiltinFunctionEntity("range", 2, BuiltinFunctions::range);
	public static void range(VM vm) {
		vm.returnFromVM(new Range(
			ObjectHelper.asInteger(vm.pop()),
			ObjectHelper.asInteger(vm.pop())
		));
	}
	
	public static final BuiltinFunctionEntity CLOCK = 
		new BuiltinFunctionEntity("clock", 0, BuiltinFunctions::clock);
	public static void clock(VM vm) {
		vm.returnFromVM(IntegerObj.valueOf(System.currentTimeMillis()));
	}
	
	public static final BuiltinFunctionEntity GET_ATTR = 
		new BuiltinFunctionEntity("getAttr", 2, BuiltinFunctions::getAttr);
	public static void getAttr(VM vm) {
		CandyObject obj = vm.pop();
		String attrStr = ObjectHelper.asString(vm.pop());
		CandyObject ret = obj.getAttr(vm, attrStr);
		if (ret == null) {
			ret = NullPointer.nil();
		}
		vm.returnFromVM(ret);
	}
	
	public static final BuiltinFunctionEntity SET_ATTR = 
		new BuiltinFunctionEntity("setAttr", 3, BuiltinFunctions::setAttr);
	public static void setAttr(VM vm) {
		CandyObject obj = vm.pop();
		String attrStr = ObjectHelper.asString(vm.pop());
		CandyObject value = vm.pop();
		obj.checkIsFrozen(attrStr);
		obj.setAttr(vm, attrStr, value);
		vm.returnFromVM(value);
	}
	
	public static final BuiltinFunctionEntity STR = 
		new BuiltinFunctionEntity("str", 1, BuiltinFunctions::str);
	public static void str(VM vm) {
		CandyObject obj = vm.pop();
		vm.returnFromVM(obj.strApiExeUser(vm));
	}
}
