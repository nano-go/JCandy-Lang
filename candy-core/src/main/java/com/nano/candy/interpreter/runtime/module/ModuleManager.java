package com.nano.candy.interpreter.runtime.module;

import com.nano.candy.interpreter.builtin.type.ModuleObj;
import com.nano.candy.interpreter.builtin.type.error.ModuleError;
import com.nano.candy.interpreter.cni.CNIEnv;

public class ModuleManager {
	
	private static final ModuleManager instance = new ModuleManager();
	
	public static ModuleManager getManager() {
		return instance;
	}
	
	private ModuleLoader loader;
	
	private ModuleManager() {
		loader = new CandyPathModuleLoader();
	}
	
	public void setModuleLoader(ModuleLoader loader) {
		this.loader = loader;
	}
	
	public ModuleObj importModule(CNIEnv env, String relativePath) {
		try {
			return loader.loadModule(env, relativePath);
		} catch (ModuleLoadingException | ModuleNotFoundException e) {
			new ModuleError(e.getMessage()).throwSelfNative();
		}
		throw new Error("Unreachable.");
	}
}
