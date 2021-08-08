package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.runtime.FileEnvironment;
import com.nano.candy.interpreter.i2.runtime.Variable;
import com.nano.candy.interpreter.i2.runtime.VariableTable;

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
	public CandyObject getName(CNIEnv env, CandyObject[] args) {
		return StringObj.valueOf(name);
	}
	
	@NativeMethod(name = "importAll")
	public CandyObject importAll(CNIEnv env, CandyObject[] args) {
		addToEnv(env.getEvaluatorEnv().getCurrentFileEnv());
		return null;
	}
}
