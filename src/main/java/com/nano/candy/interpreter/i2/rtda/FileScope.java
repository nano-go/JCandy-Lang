package com.nano.candy.interpreter.i2.rtda;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.vm.CompiledFileInfo;
import java.util.Collection;
import java.util.HashMap;

/**
 * This is the top scope of a Candy source file.
 */
public class FileScope {

	private CompiledFileInfo compiledFileInfo;
	private HashMap<String, Variable> vars;

	protected FileScope(CompiledFileInfo compiledFileInfo) {
		this.compiledFileInfo = compiledFileInfo;
		this.vars = new HashMap<>();
	}
	
	public CompiledFileInfo getCompiledFileInfo() {
		return compiledFileInfo;
	}

	public void setVar(String name, CandyObject value) {
		vars.put(name, Variable.getVariable(name, value));
	}

	public CandyObject getVar(String name) {
		Variable variable = vars.get(name);
		if (variable != null) return variable.getValue();
		return null;
	}
	
	public Variable getVariable(String name) {
		return vars.get(name);
	}
	
	public void defineCallable(CallableObj callableObj) {
		setVar(callableObj.declredName(), callableObj);
	}
	
	public void defineClass(CandyClass clazz) {
		setVar(clazz.getCandyClassName(), clazz);
	}
	
	public void defineVeriable(Variable variable) {
		vars.put(variable.getName(), variable);
	}
	
	public ModuleObj generateModuleObject() {
		return new ModuleObj(
			compiledFileInfo.getAbsPath(), vars
		);
	}
	
	public Collection<Variable> getVariables() {
		return vars.values();
	}
}
