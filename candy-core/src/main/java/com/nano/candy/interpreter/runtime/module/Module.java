package com.nano.candy.interpreter.runtime.module;

/**
 * This class represent a module object.
 */
import com.nano.candy.interpreter.builtin.type.error.IOError;
import java.io.File;
import java.io.IOException;

public class Module {
	
	/**
	 * Returns a only representation of the specified file.
	 */
	public static String getOnlyIdentifier(File f) throws IOException {
		return f.getCanonicalPath();
	}
	
	private static File[] toFiles(String[] paths) {
		File[] files = new File[paths.length];
		for (int i = 0; i < paths.length; i ++) {
			files[i] = new File(paths[i]);
		}
		return files;
	}
	
	/**
	 * A module object consists of multiple source files.
	 */
	private File[] sourceFiles;
	
	private String name;
	
	/**
	 * The only representation of this module.
	 */
	private String moduleIdentifier;
	
	/**
	 * This module path. a directory or a file.
	 */
	private String modulePath;
	
	/**
	 * True means this module consists of multiple source files.
	 */
	private boolean isModuleSet;
	
	public Module(String name, String modulePath,
	              boolean isModuleSet, String[] paths) throws IOException {
		this(name, modulePath, isModuleSet, toFiles(paths));
	}
	
	public Module(String name, String modulePath,
	              boolean isModuleSet, File[] files) throws IOException
	{
		this.name = name;
		this.moduleIdentifier = getOnlyIdentifier(new File(modulePath));
		this.modulePath = modulePath;
		this.isModuleSet = isModuleSet;
		this.sourceFiles = files;
	}
	
	public String getSubFileIdentifier(int i) {
		try {
			return getOnlyIdentifier(sourceFiles[i]);
		} catch (IOException e) {
			new IOError(e).throwSelfNative();
			throw new Error("Unrachable.");
		}
	}
	
	public File getSubFile(int index) {
		return sourceFiles[index];
	}
	
	public int getSubFilesCount() {
		return sourceFiles.length;
	}
	
	public String getModuleIdentifier() {
		return moduleIdentifier;
	}
	
	public String getModulePath() {
		return modulePath;
	}
	
	public boolean isModuleSet() {
		return isModuleSet;
	}
	
	public String getName() {
		return name;
	}
}
