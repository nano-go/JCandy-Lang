package com.nano.candy.interpreter.i2.runtime.module;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import com.nano.candy.interpreter.i2.runtime.CompiledFileInfo;
import com.nano.candy.interpreter.i2.runtime.Variable;
import com.nano.candy.interpreter.i2.tool.Compiler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ModuleLoader {

	private Map<SourceFileInfo, ModuleObj> fileModuleObjectCache;
	private Map<String, ModuleObj> moduleObjectCache;

	public ModuleLoader() {
		this.fileModuleObjectCache = new ConcurrentHashMap<>();
		this.moduleObjectCache = new ConcurrentHashMap<>();
	}

	/**
	 * Finds the module with the specified relative path.
	 *
	 * @throws ModuleNotFoundException 
	 *         If the moudle could not be found.
	 */
	protected abstract Module findModule(CNIEnv env, String relativePath) 
		throws ModuleNotFoundException;
	
	/**
	 * Loads the module with the specified relative path.
	 *
	 * @throws ModuleNotFoundException 
	 *         If the moudle could not be found.
	 *
	 * @throws ModuleLoadingException
	 *         If fail to load the module.
	 */
	public ModuleObj loadModule(CNIEnv env, String relativePath) 
		throws ModuleLoadingException, ModuleNotFoundException {
		Module module = findModule(env, relativePath);	
		return loadModule(env, module);
	}

	protected final synchronized ModuleObj loadModule(CNIEnv env, Module module) 
		throws ModuleLoadingException {
		ModuleObj moduleObj = moduleObjectCache.get(module.getModulePath());
		if (moduleObj != null) {
			return moduleObj;
		}
		verifyModule(env, module);
		ModuleObj[] moduleObjects = compileModule(env, module);
		moduleObj = mergeModules(module.getName(), moduleObjects);
		moduleObjectCache.put(module.getModulePath(), moduleObj);
		return moduleObj;
	}
	
	protected void verifyModule(CNIEnv env, Module module) 
		throws ModuleLoadingException {
		for (int i = 0; i < module.getFileCount(); i ++) {
			if (module.getSourceFile(i).isRunning()) {
				throw new ModuleLoadingException
					("cyclic import, in " + 
					 env.getEvaluatorEnv().getCurRunningFile().getSimpleName() +
					  " import the running module " + module.getName());
			}
		}
	}
	
	private ModuleObj[] compileModule(CNIEnv env, Module module) {
		final int fileCount = module.getFileCount();
		ModuleObj[] moduleObjects = new ModuleObj[fileCount];
		for (int i = 0; i < fileCount; i ++) {
			SourceFileInfo srcFile = module.getSourceFile(i);
			ModuleObj fileModuleObj = fileModuleObjectCache.get(srcFile);
			if (fileModuleObj == null) {
				fileModuleObj = compileSourceFile(env, srcFile);		
				fileModuleObjectCache.put(srcFile, fileModuleObj);
			}
			moduleObjects[i] = fileModuleObj;
		}
		return moduleObjects;
	}

	private ModuleObj compileSourceFile(CNIEnv env, SourceFileInfo srcFile) {
		CompiledFileInfo compiledFile =
			Compiler.compile(srcFile.getFile(), 
			env.getEvaluatorEnv().getOptions(), 
			true
		);
		return env.getEvaluator().eval(compiledFile);
	}
	
	private ModuleObj mergeModules(String moduleName, 
	                               ModuleObj[] moduleObjects) 
	{
		ModuleObj moduleObj = new ModuleObj
			(moduleName, new HashMap<String, Variable>());
		for (ModuleObj submoduleObj : moduleObjects) {
			submoduleObj.defineTo(moduleObj);
		}
		return moduleObj;
	}
}
