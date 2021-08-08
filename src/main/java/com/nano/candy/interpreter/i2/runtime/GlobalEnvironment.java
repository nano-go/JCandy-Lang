package com.nano.candy.interpreter.i2.runtime;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.runtime.CompiledFileInfo;
import com.nano.candy.interpreter.i2.runtime.module.SourceFileInfo;
import java.util.HashMap;

public class GlobalEnvironment {
	
	private HashMap<String, FileEnvironment> fileEnvCache;
	private FileEnvironment currentFileEnv;
	
	public GlobalEnvironment() {
		this.fileEnvCache = new HashMap<>();
		this.currentFileEnv = null;
	}
	
	private FileEnvironment generateFileEnvrionment(CompiledFileInfo compiledFileInfo) {
		FileEnvironment fenv;
		String absPath;
		if (compiledFileInfo.isRealFile()) {
			absPath = SourceFileInfo.get(compiledFileInfo.getFile())
				.getFile().getAbsolutePath();
		} else {
			absPath = compiledFileInfo.getAbsPath();
		}
		fenv = fileEnvCache.get(absPath);
		if (fenv == null) {
			fenv = new FileEnvironment(compiledFileInfo);
			fileEnvCache.put(absPath, fenv);
		}
		return fenv;
	}
	
	public FileEnvironment getCurrentFileEnv() {
		return currentFileEnv;
	}
	
	public void setCurrentFileEnv(FileEnvironment env) {
		this.currentFileEnv = env;
	}
	
	public void setCurrentFileEnv(CompiledFileInfo compiledFileInfo) {
		this.currentFileEnv = generateFileEnvrionment(compiledFileInfo);
	}
	
	public void removeFileScope() {
		this.currentFileEnv = null;
	}
	
	public String getVariableName(int index) {
		return currentFileEnv
			.getVariableTable().getVariableName(index);
	}
	
	public CandyObject getVariableValue(int index) {
		Variable v = getVariable(index);
		return v != null ? v.getValue() : null;
	}
	
	public CandyObject getVariableValue(String name) {
		Variable v = getVariable(name);
		return v != null ? v.getValue() : null;
	}
	
	public Variable getVariable(int index) {
		VariableTable vars = currentFileEnv.getVariableTable();
		Variable v = vars.getVariable(index);
		return v != null ? v : 
			BuiltinVariables.getVariable(vars.getVariableName(index));
	}
	
	public Variable getVariable(String name) {
		Variable v = currentFileEnv.getVariableTable().getVariable(name);
		return v != null ? v : BuiltinVariables.getVariable(name);
	}
	
	public void setVariable(int index, CandyObject value) {
		currentFileEnv.getVariableTable().setVariable(index, value);
	}
	
	public void setVariable(String name, CandyObject value) {
		currentFileEnv.getVariableTable().defineVariable(name, value);
	}
	
	public boolean setVariableIfExists(int index, CandyObject value) {
		return currentFileEnv.getVariableTable()
			.setVariableIfExists(index, value);
	}
}
