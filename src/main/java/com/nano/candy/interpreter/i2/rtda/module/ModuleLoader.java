package com.nano.candy.interpreter.i2.rtda.module;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.rtda.Variable;
import com.nano.candy.interpreter.i2.tool.Compiler;
import com.nano.candy.interpreter.i2.vm.CompiledFileInfo;
import com.nano.candy.interpreter.i2.vm.VM;
import java.util.HashMap;

public abstract class ModuleLoader {

	private HashMap<SourceFileInfo, ModuleObj> fileModuleObjectCache;
	private HashMap<String, ModuleObj> moduleObjectCache;

	public ModuleLoader() {
		this.fileModuleObjectCache = new HashMap<>();
		this.moduleObjectCache = new HashMap<>();
	}

	/**
	 * Finds the module with the specified relative path.
	 *
	 * @throws ModuleNotFoundException 
	 *         If the moudle could not be found.
	 */
	protected abstract Module findModule(VM vm, String relativePath) 
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
	public ModuleObj loadModule(VM vm, String relativePath) 
		throws ModuleLoadingException, ModuleNotFoundException {
		Module module = findModule(vm, relativePath);	
		return loadModule(vm, module);
	}

	protected final ModuleObj loadModule(VM vm, Module module) 
		throws ModuleLoadingException {
		ModuleObj moduleObj = moduleObjectCache.get(module.getModulePath());
		if (moduleObj != null) {
			return moduleObj;
		}
		verifyModule(vm, module);
		ModuleObj[] moduleObjects = compileModule(vm, module);
		moduleObj = mergeModules(module.getName(), moduleObjects);
		moduleObjectCache.put(module.getModulePath(), moduleObj);
		return moduleObj;
	}
	
	protected void verifyModule(VM vm, Module module) 
		throws ModuleLoadingException {
		for (int i = 0; i < module.getFileCount(); i ++) {
			if (module.getSourceFile(i).isRunning()) {
				throw new ModuleLoadingException
					("cyclic import, in " + 
					 vm.getCurRunningFile().getSimpleName() +
					  " import the running module " + module.getName());
			}
		}
	}
	
	private ModuleObj[] compileModule(VM vm, Module module) {
		final int fileCount = module.getFileCount();
		ModuleObj[] moduleObjects = new ModuleObj[fileCount];
		for (int i = 0; i < fileCount; i ++) {
			SourceFileInfo srcFile = module.getSourceFile(i);
			ModuleObj fileModuleObj = fileModuleObjectCache.get(srcFile);
			if (fileModuleObj == null) {
				fileModuleObj = compileSourceFile(vm, srcFile);		
				fileModuleObjectCache.put(srcFile, fileModuleObj);
			}
			moduleObjects[i] = fileModuleObj;
		}
		return moduleObjects;
	}

	private ModuleObj compileSourceFile(VM vm, SourceFileInfo srcFile) {
		CompiledFileInfo compiledFile =
			Compiler.compile(srcFile.getFile(), vm.getOptions(), true);
		vm.loadFile(compiledFile);
		return vm.run();
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
