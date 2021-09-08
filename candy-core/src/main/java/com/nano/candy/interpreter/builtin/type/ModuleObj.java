package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.ModuleObj;
import com.nano.candy.interpreter.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeMethod;
import com.nano.candy.interpreter.runtime.FileEnvironment;
import com.nano.candy.interpreter.runtime.Variable;
import com.nano.candy.interpreter.runtime.VariableTable;

@NativeClass(name = "Module")
public class ModuleObj extends CandyObject {
	
	public static final CandyClass MOUDLE_CLASS = 
		NativeClassRegister.generateNativeClass(ModuleObj.class);
	
	private String name;
	private VariableTable attrs;
	public ModuleObj(String name, VariableTable attrs) {
		super(MOUDLE_CLASS);
		this.name = name;
		this.attrs = attrs;
	}
	
	@Override
	public CandyObject getAttr(CNIEnv env, String attr) {
		Variable variable = attrs.getVariable(attr);
		if (variable != null) {
			return variable.getValue();
		}
		CandyObject val = super.getAttr(env, attr);
		if (val != null) {
			return val;
		}
		new AttributeError(
			"The module '%s' has no attribute '%s'.", name, attr
		).throwSelfNative();
		throw new Error();
	}

	@Override
	public CandyObject setAttr(CNIEnv env, String attr, CandyObject ref) {
		Variable variable = attrs.getVariable(attr);
		if (variable != null) {
			variable.setValue(ref);
			return ref;
		}
		new AttributeError(
			"The module '%s' has no attribute '%s'.", name, attr
		).throwSelfNative();
		throw new Error();
	}
	
	public void defineTo(ModuleObj moudleObj) {
		moudleObj.attrs.defineAll(this.attrs);
	}
	
	public void addToEnv(FileEnvironment env) {
		env.getVariableTable().defineAll(this.attrs);
	}

	@Override
	public CandyObject iterator(CNIEnv env) {
		return new IteratorObj.VarableIterator(attrs.getVariables().iterator());
	}
	
	public String getName() {
		return name;
	}
	
	@NativeMethod(name = "name")
	public CandyObject getName(CNIEnv env) {
		return StringObj.valueOf(name);
	}
	
	@NativeMethod(name = "importAll")
	public CandyObject importAll(CNIEnv env) {
		addToEnv(env.getCurrentFileEnv());
		return null;
	}
}
