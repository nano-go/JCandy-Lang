package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.vm.VM;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@BuiltinClass("Moudle")
public class MoudleObj extends BuiltinObject {
	
	public static final CandyClass MOUDLE_CLASS = BuiltinClassFactory.generate(
		MoudleObj.class);
	
	private String name;
	private HashMap<String, CandyObject> attrs;
	public MoudleObj(String name, HashMap<String, CandyObject> attrs) {
		super(MOUDLE_CLASS);
		this.name = name;
		this.attrs = attrs;
	}

	@Override
	public CandyObject getAttr(VM vm, String attr) {
		CandyObject obj = attrs.get(attr);
		if (obj == null) {
			return super.getAttr(vm, attr);
		}
		return obj;
	}

	@Override
	public CandyObject iterator() {
		return new IteratorObj.MapIterator(attrs.entrySet().iterator());
	}
	
	public String getName() {
		return name;
	}
	
	@BuiltinMethod(name = "name")
	public void getName(VM vm) {
		vm.returnFromVM(StringObj.valueOf(name));
	}
}