package com.nano.candy.interpreter.i2.rtda.module;

import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import com.nano.candy.sys.CandySystem;
import com.nano.candy.utils.CandyFileFilter;
import java.io.File;
import java.io.IOException;

/**
 * The default loader could load modules from the '$CANDY_HOME/libs' and
 * the directory where the current running file is in.
 */
public class CandyPathModuleLoader extends ModuleLoader {
	
	private static void checkModule(Module module, 
	                                String relativePath) 
		throws ModuleNotFoundException {
		if (module == null) {
			throw new ModuleNotFoundException
				("The module " + relativePath + " could not be found.");
		}
	}
	
	@Override
	protected Module findModule(VM vm, String relativePath) 
		throws ModuleNotFoundException {
		Module module = findFrom(vm.getCurrentDirectory(), relativePath);
		if (module != null) {
			return module;
		}
		String libsPath = CandySystem.getCandyLibsPath();
		if (libsPath != null) {
			module = findFrom(libsPath, relativePath);
		}
		checkModule(module, relativePath);
		return module;
	}

	private Module findFrom(String envDirectory, String relativePath) {
		Module module = attemptToFindSrcFile(envDirectory, relativePath);
		if (module == null) {
			module = attemptToFindDirectory(envDirectory, relativePath);
		}
		return module;
	}

	/**
	 * Attempts to find a single source file as a module.
	 *
	 * @return the module generated by the found source file or null
	 *         if not found.
	 */
	private Module attemptToFindSrcFile(String envDirectory, 
	                                    String relativePath) {
		String srcFilePath = getSourceFileName(relativePath);
		File srcFile = new File(envDirectory, srcFilePath);
		return srcFile.isFile() ? generteFileModule(srcFile) : null;
	}
	
	private String getSourceFileName(String path) {
		if (!path.endsWith("." + CandySystem.FILE_SUFFIX)) {	
			return path + "." + CandySystem.FILE_SUFFIX;
		}
		return path;
	}
	
	/**
	 * Attempts to find the files with the suffix ".cd" or the
	 * {@link Names#MOUDLE_FILE_NAME} source file as a module in a directory.
	 *
	 * @return the module generated by the found source files or null
	 *         if not found.
	 */
	private Module attemptToFindDirectory(String envDirectory, String path) {
		File directory = new File(envDirectory, path);
		if (!directory.isDirectory()) {
			return null;
		}
		File moduleSrcFile = new File(directory, Names.MOUDLE_FILE_NAME);
		if (moduleSrcFile.isFile()) {
			return generteFileModule(directory.getName(), moduleSrcFile);
		}
		return generteDirectoryModule(directory);
	}

	private Module generteDirectoryModule(File directory) {
		File[] subSourceFiles = 
			directory.listFiles(CandyFileFilter.CANDY_FILE_FILTER);
		if (subSourceFiles.length == 0) {
			return null;
		}
		try {
			return new Module(
				directory.getName(),
				directory.getAbsolutePath(),
				true,
				subSourceFiles
			);
		} catch (IOException e) {
			return null;
		}
	}
	
	private Module generteFileModule(File file) {
		return generteFileModule(file.getName(), file);
	}
	
	private Module generteFileModule(String name, File file) {
		try {
			return new Module(
				name,
				file.getAbsolutePath(),
				false,
				new String[] {file.getAbsolutePath()}
			);
		} catch (IOException e) {
			return null;
		}
	}
	
}
