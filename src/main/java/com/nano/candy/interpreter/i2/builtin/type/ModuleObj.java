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
import java.util.HashMap;
import java.util.Map;

@NativeClass(name = "Module")
public class ModuleObj extends CandyObject {
	
	public static final CandyClass MOUDLE_CLASS = 
		NativeClassRegister.generateNativeClass(ModuleObj.class);
	
	private String name;
	private Map<String, Variable> attrs;
	public ModuleObj(String name, Map<String, Variable> attrs) {
		super(MOUDLE_CLASS);
		this.name = name;
		this.attrs = attrs;
	}
	
	@Override
	public CandyObject getAttr(CNIEnv env, String attr) {
		Variable variable = attrs.get(attr);
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
	public CandyObject iterator(CNIEnv env) {
		return new IteratorObj.VarableIterator(attrs.values().iterator());
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
