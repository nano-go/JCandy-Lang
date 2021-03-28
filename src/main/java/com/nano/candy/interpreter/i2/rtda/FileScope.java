package com.nano.candy.interpreter.i2.rtda;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.rtda.moudle.CompiledFileInfo;
import java.util.HashMap;

/**
 * This is the top scope of a Candy source file.
 */
public class FileScope {

	public CompiledFileInfo compiledFileInfo;
	public HashMap<String, CandyObject> vars;

	protected FileScope(CompiledFileInfo compiledFileInfo) {
		this.compiledFileInfo = compiledFileInfo;
		this.vars = new HashMap<>();
	}

	public void setVar(String name, CandyObject value) {
		vars.put(name, value);
	}

	public CandyObject getVar(String name) {
		return vars.get(name);
	}
}
