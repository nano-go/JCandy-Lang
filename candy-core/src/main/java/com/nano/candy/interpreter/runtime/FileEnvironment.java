package com.nano.candy.interpreter.runtime;

import com.nano.candy.interpreter.builtin.type.ModuleObj;
import com.nano.candy.interpreter.runtime.CompiledFileInfo;

/**
 * An environment stores all variables in a Candy source file.
 */
public class FileEnvironment {

	private CompiledFileInfo compiledFileInfo;
	private VariableTable variableTable;

	protected FileEnvironment(CompiledFileInfo compiledFileInfo) {
		this.variableTable = new VariableTable(
			compiledFileInfo.getChunk().getGlobalVarNames(),
			compiledFileInfo.getChunk().getGlobalVarTable()
		);
		this.compiledFileInfo = compiledFileInfo;
	}
	
	public CompiledFileInfo getCompiledFileInfo() {
		return compiledFileInfo;
	}
	
	public VariableTable getVariableTable() {
		return variableTable;
	}
	
	public ModuleObj generateModuleObject() {
		return new ModuleObj(
			compiledFileInfo.getAbsPath(), variableTable
		);
	}
}
