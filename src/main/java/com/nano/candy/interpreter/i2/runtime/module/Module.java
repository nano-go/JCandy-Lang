package com.nano.candy.interpreter.i2.runtime.module;

/**
 * This class represent a module object.
 */
import java.io.File;
import java.io.IOException;

public class Module {
	
	private static File[] toFiles(String[] paths) {
		File[] files = new File[paths.length];
		for (int i = 0; i < paths.length; i ++) {
			files[i] = new File(paths[i]);
		}
		return files;
	}
	
	/**
	 * A module object maybe consist of multiple source files.
	 */
	private SourceFileInfo[] sourceFiles;
	private String name;
	private String modulePath;
	private boolean isModuleSet;
	
	public Module(String name, String modulePath,
	              boolean isModuleSet, String[] paths) throws IOException {
		this(name, modulePath, isModuleSet, toFiles(paths));
	}
	
	public Module(String name, String modulePath,
	              boolean isModuleSet, File[] files) throws IOException
	{
		this.name = name;
		this.modulePath = new File(modulePath).getCanonicalPath();
		this.isModuleSet = isModuleSet;
		this.sourceFiles = new SourceFileInfo[files.length];
		for (int i = 0; i < files.length; i ++) {
			sourceFiles[i] = SourceFileInfo.get(files[i]);
		}
	}
	
	public SourceFileInfo getSourceFile(int index) {
		return sourceFiles[index];
	}
	
	public int getFileCount() {
		return sourceFiles.length;
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
