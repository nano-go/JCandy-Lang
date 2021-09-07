package com.nano.candy.interpreter.runtime.module;
import com.nano.candy.interpreter.builtin.type.ModuleObj;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.runtime.CompiledFileInfo;
import com.nano.candy.interpreter.runtime.RuntimeCompiler;
import com.nano.candy.interpreter.runtime.VariableTable;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to load modules.
 *
 * <p>In Candy, A module consists of one or more source files. Any Candy source
 * file may be a module.
 *
 * <p>ModuleLoader provides a method used for loading modules through a path,
 * and the ModuleObject cache that avoids loading the same module twice.
 *
 * @see CandyPathModuleLoader
 */
public abstract class ModuleLoader {
	
	private Map<String, ModuleObj> moduleObjectCache;
	
	private Set<String> runningModules;

	public ModuleLoader() {
		this.moduleObjectCache = new ConcurrentHashMap<>();
		this.runningModules = Collections.synchronizedSet(new HashSet<String>());
	}
	
	protected final boolean isRunning(String moduleIdentifier) {
		return runningModules.contains(moduleIdentifier);
	}

	protected final void markRunning(String moduleIdentifier) {
		runningModules.add(moduleIdentifier);
	}

	protected final void unmarkRunning(String moduleIdentifier) {
		runningModules.remove(moduleIdentifier);
	}

	/**
	 * Finds a module through the specified relative path.
	 *
	 * @throws ModuleNotFoundException 
	 *         If the moudle could not be found.
	 *
	 * @return A module. Can't be null.
	 */
	protected abstract Module findModule(CNIEnv env, String relativePath) 
		throws ModuleNotFoundException;
	
	/**
	 * Finds a module and loads it.
	 *
	 * @throws ModuleNotFoundException 
	 *         If the moudle could not be found.
	 *
	 * @throws ModuleLoadingException
	 *         If fail to load the module.
	 */
	public ModuleObj loadModule(CNIEnv env, String relativePath) throws ModuleLoadingException, ModuleNotFoundException {
		Module module = findModule(env, relativePath);	
		return loadModule(env, module);
	}

	protected final synchronized ModuleObj loadModule(CNIEnv env, Module module) throws ModuleLoadingException {
		// Use the module identifier as the key of the cache.
		ModuleObj moduleObj = 
			moduleObjectCache.get(module.getModuleIdentifier());
		if (moduleObj != null) {
			return moduleObj;
		}
		checkModule(env, module);
		return runModule(env, module);
	}
	
	/**
	 * Check if the module is running. E.g A -> B -> A.
	 */
	private void checkModule(CNIEnv env, Module module) throws ModuleLoadingException {
		boolean cyclic = false;
		final int subfilesCount = module.getSubFilesCount();
		for (int i = 0; i < subfilesCount; i ++) {
			if (isRunning(module.getSubFileIdentifier(i))) {
				cyclic = true;
				break;
			}
		}
		if (cyclic || isRunning(module.getModuleIdentifier())) {
			throw new ModuleLoadingException
				("cyclic importation. in " + 
				 env.getCurRunningFile().getSimpleName() +
				 " imports the running module " + module.getName());
		}
	}
	
	private ModuleObj runModule(CNIEnv env, Module module) {
		if (module.isModuleSet()) {
			// It means this module consists of multiple source files.
			ModuleObj[] subModuleObjects = runModuleSet(env, module);
			ModuleObj moduleObj = mergeModules(module.getName(), subModuleObjects);
			moduleObjectCache.put(module.getName(), moduleObj);
			return moduleObj;
		}
		return runSingleFileModule(env, module);
	}
	
	private ModuleObj runSingleFileModule(CNIEnv env, Module module) {
		String moduleIdentifier = module.getModuleIdentifier();
		ModuleObj moduleObj = runSourceFile(
			env, moduleIdentifier, new File(module.getModulePath()));
		moduleObjectCache.put(moduleIdentifier, moduleObj);
		return moduleObj;
	}
	
	private ModuleObj[] runModuleSet(CNIEnv env, Module module) {
		final int fileCount = module.getSubFilesCount();
		ModuleObj[] subModuleObjs = new ModuleObj[fileCount];
		
		for (int i = 0; i < fileCount; i ++) {
			String subModuleIdentifier = module.getSubFileIdentifier(i);
			ModuleObj subModuleObj = moduleObjectCache.get(subModuleIdentifier);
			if (subModuleObj == null) {
				subModuleObj = runSourceFile(
					env, subModuleIdentifier, module.getSubFile(i));		
				moduleObjectCache.put(subModuleIdentifier, subModuleObj);
			}
			subModuleObjs[i] = subModuleObj;
		}
		return subModuleObjs;
	}
	
	private ModuleObj runSourceFile(CNIEnv env, String id, File srcFile) {
		CompiledFileInfo compiledFile = RuntimeCompiler.compile(
			srcFile, env.getOptions(), true
		);
		try {
			markRunning(id);
			return env.getEvaluator().eval(compiledFile);
		} finally {
			unmarkRunning(id);
		}
	}
	
	private ModuleObj mergeModules(String moduleName, 
	                               ModuleObj[] moduleObjects) 
	{
		ModuleObj moduleObj = new ModuleObj(moduleName, new VariableTable(32));
		for (ModuleObj submoduleObj : moduleObjects) {
			submoduleObj.defineTo(moduleObj);
		}
		return moduleObj;
	}
}
