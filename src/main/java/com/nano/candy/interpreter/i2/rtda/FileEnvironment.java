package com.nano.candy.interpreter.i2.rtda;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.vm.CompiledFileInfo;
import java.util.Collection;
import java.util.HashMap;

/**
 * An environment stores all variables in a Candy source file.
 */
public class FileEnvironment {

	private CompiledFileInfo compiledFileInfo;
	private HashMap<String, Variable> vars;

	protected FileEnvironment(CompiledFileInfo compiledFileInfo) {
		this.compiledFileInfo = compiledFileInfo;
		this.vars = new HashMap<>(BuiltinVariables.getVariables());
	}
	
	public CompiledFileInfo getCompiledFileInfo() {
		return compiledFileInfo;
	}

	public void setVariable(String name, CandyObject value) {
		vars.put(name, Variable.getVariable(name, value));
	}

	public CandyObject getVariableValue(String name) {
		Variable variable = vars.get(name);
		return variable != null ? variable.getValue() : null;
	}
	
	public Variable getVariable(String name) {
		return vars.get(name);
	}
	
	public void defineCallable(CallableObj callableObj) {
		setVariable(callableObj.declaredName(), callableObj);
	}
	
	public void defineClass(CandyClass clazz) {
		setVariable(clazz.getCandyClassName(), clazz);
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
