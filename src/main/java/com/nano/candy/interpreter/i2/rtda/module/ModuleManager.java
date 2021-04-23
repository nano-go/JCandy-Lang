package com.nano.candy.interpreter.i2.rtda.module;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.builtin.type.error.ModuleError;
import com.nano.candy.interpreter.i2.vm.VM;

public class ModuleManager {
	
	private ModuleLoader loader;
	
	public ModuleManager() {
		loader = new CandyPathModuleLoader();
	}
	
	public void setModuleLoader(ModuleLoader loader) {
		this.loader = loader;
	}
	
	public ModuleObj importModule(VM vm, String relativePath) {
		try {
			return loader.loadModule(vm, relativePath);
		} catch (ModuleLoadingException | ModuleNotFoundException e) {
			new ModuleError(e.getMessage()).throwSelfNative();
		}
		throw new Error("Unreachable.");
	}
}
