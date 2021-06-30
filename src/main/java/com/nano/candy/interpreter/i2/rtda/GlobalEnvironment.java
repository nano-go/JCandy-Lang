package com.nano.candy.interpreter.i2.rtda;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.rtda.module.SourceFileInfo;
import com.nano.candy.interpreter.i2.vm.CompiledFileInfo;
import java.util.HashMap;

public class GlobalEnvironment {
	
	private HashMap<String, FileEnvironment> fileEnvCache;
	private FileEnvironment currentFileEnv;
	
	public GlobalEnvironment() {
		fileEnvCache = new HashMap<>();
		currentFileEnv = null;
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
	
	public CandyObject getVariableValue(String name) {
		return currentFileEnv.getVariableValue(name);
	}
	
	public Variable getVariable(String name) {
		return currentFileEnv.getVariable(name);
	}
	
	public void setVariable(String name, CandyObject value) {
		currentFileEnv.setVariable(name, value);
	}
}
