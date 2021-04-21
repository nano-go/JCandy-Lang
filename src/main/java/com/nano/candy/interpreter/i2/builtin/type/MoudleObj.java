package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.MoudleObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.rtda.Variable;
import com.nano.candy.interpreter.i2.vm.VM;
import java.util.HashMap;

@NativeClass(name = "Moudle")
public class MoudleObj extends BuiltinObject {
	
	public static final CandyClass MOUDLE_CLASS = 
		NativeClassRegister.generateNativeClass(MoudleObj.class);
	
	private String name;
	private HashMap<String, Variable> attrs;
	public MoudleObj(String name, HashMap<String, Variable> attrs) {
		super(MOUDLE_CLASS);
		this.name = name;
		this.attrs = attrs;
	}

	@Override
	public CandyObject getAttr(VM vm, String attr) {
		CandyObject obj = attrs.get(attr).getValue();
		if (obj == null) {
			return super.getAttr(vm, attr);
		}
		return obj;
	}

	@Override
	public CandyObject iterator(VM vm) {
		return new IteratorObj.VarableIterator(attrs.values().iterator());
	}
	
	public String getName() {
		return name;
	}
	
	@NativeMethod(name = "name")
	public CandyObject getName(VM vm, CandyObject[] args) {
		return StringObj.valueOf(name);
	}
}
