package com.nano.candy.interpreter.i2.rtda.module;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.tool.Compiler;
import com.nano.candy.interpreter.i2.vm.CompiledFileInfo;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.sys.CandySystem;
import java.io.File;
import java.util.HashMap;

public class ModuleManager {
	
	private HashMap<SourceFileInfo, ModuleObj> importedModules;
	
	public ModuleManager() {
		importedModules = new HashMap<>();
	}
	
	public static File findSourceFile(String env, String path) {
		int index = path.lastIndexOf("/");
		String name = path;
		if (index > 0) {
			name = path.substring(index + 1);
		}
		if (name.length() > 0 && name.lastIndexOf(".") < 0) {
			path += "." + CandySystem.FILE_SUFFIX;
		}
		return new File(env, path);
	}
	
    public ModuleObj importFile(VM vm, String relativePath) {
		SourceFileInfo srcFile = SourceFileInfo.get(
			findSourceFile(vm.getCurrentDirectory(), relativePath)
		);
		ModuleObj moduleObj = checkSrcFile(srcFile);
		if (moduleObj == null) {
			moduleObj = runFile(vm, srcFile);
			importedModules.put(srcFile, moduleObj);
			srcFile.markImported();
		}
		return moduleObj;
	}
	
	private ModuleObj checkSrcFile(SourceFileInfo srcFile) {
		if (srcFile.isRunning()) {
			new NativeError("Cyclic import.").throwSelfNative();
		}
		return importedModules.get(srcFile);
	}

	private ModuleObj runFile(VM vm, SourceFileInfo srcFile) {
		CompiledFileInfo compiledFile = Compiler.compile(
			srcFile.getFile(), vm.getOptions(), true);
		vm.loadFile(compiledFile);
		return vm.run();
	}
	
}
