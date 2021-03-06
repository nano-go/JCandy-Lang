package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.rtda.FileEnvironment;
import com.nano.candy.interpreter.i2.rtda.Variable;
import com.nano.candy.interpreter.i2.vm.VM;
import java.util.HashMap;

@NativeClass(name = "Module")
public class ModuleObj extends CandyObject {
	
	public static final CandyClass MOUDLE_CLASS = 
		NativeClassRegister.generateNativeClass(ModuleObj.class);
	
	private String name;
	private HashMap<String, Variable> attrs;
	public ModuleObj(String name, HashMap<String, Variable> attrs) {
		super(MOUDLE_CLASS);
		this.name = name;
		this.attrs = attrs;
	}
	
	@Override
	public CandyObject getAttr(VM vm, String attr) {
		Variable variable = attrs.get(attr);
		if (variable != null) {
			return variable.getValue();
		}
		CandyObject val = super.getAttr(vm, attr);
		if (val != null) {
			return val;
		}
		new AttributeError(
			"The module '%s' has no attribute '%s'.", name, attr
		).throwSelfNative();
		throw new Error();
	}

	@Override
	public CandyObject setAttr(VM vm, String attr, CandyObject ref) {
		Variable variable = attrs.get(attr);
		if (variable != null) {
			variable.setValue(ref);
			return ref;
		}
		new AttributeError(
			"The module '%s' has no attribute '%s'.", name, attr
		).throwSelfNative();
		throw new Error();
	}
	
	public void defineTo(HashMap<String, Variable> vars) {
		vars.putAll(attrs);
	}
	
	public void defineTo(ModuleObj moudleObj) {
		moudleObj.attrs.putAll(this.attrs);
	}
	
	public void addToEnv(FileEnvironment env) {
		for (Variable v : attrs.values()) {
			env.defineVeriable(v);
		}
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
	
	@NativeMethod(name = "addToCurEnv")
	public CandyObject addToCurEnv(VM vm, CandyObject[] args) {
		addToEnv(vm.getCurrentFileEnv());
		return null;
	}
}
