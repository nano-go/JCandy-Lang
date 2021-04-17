package com.nano.candy.interpreter.i2.builtin.functions;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.functions.BuiltinFunctions;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.Range;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
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
	
	public static final BuiltinFunctionEntity IMPORT = 
		new BuiltinFunctionEntity("importFile", 1, BuiltinFunctions::importFile);
	public static void importFile(VM vm) {
		String filePath = ObjectHelper.asString(vm.pop());
		vm.returnFromVM(vm.getMoudleManager().importFile(vm, filePath));
	}
	
	public static final BuiltinFunctionEntity CMD_ARGS = 
		new BuiltinFunctionEntity("cmd_args", 0, BuiltinFunctions::cmd_args);
	public static void cmd_args(VM vm) {
		CandyObject[] args = new CandyObject[vm.getOptions().getArgs().length];
		int i = 0;
		for (String arg : vm.getOptions().getArgs()) {
			args[i ++] = StringObj.valueOf(arg);
		}
		vm.returnFromVM(new ArrayObj(args));
	}
}
