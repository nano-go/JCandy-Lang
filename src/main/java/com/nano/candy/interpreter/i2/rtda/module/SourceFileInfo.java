package com.nano.candy.interpreter.i2.rtda.module;

import com.nano.candy.interpreter.i2.builtin.type.error.IOError;
import com.nano.candy.interpreter.i2.vm.CarrierErrorException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SourceFileInfo {
	
	private static final HashMap<String, SourceFileInfo> sourceFiles = new HashMap<>();
	
	public static boolean markRunning(String fileName) {
		try {
			get(new File(fileName)).markRunning();
			return true;
		} catch (CarrierErrorException e) {
			return false;
		}
	}
	
	public static boolean unmarkRunning(String fileName) {
		try {
			get(new File(fileName)).unmarkRunning();
			return true;
		} catch (CarrierErrorException e) {
			return false;
		}
	}
	
	/**
	 * Returns the only source file info of the specified file.
	 *
	 * @return The source file info.
	 */
	public static SourceFileInfo get(File file) {
		IOError.checkCandySourceFile(file);
		try {
			String canonicalPath = file.getCanonicalPath();
			return getOnlyFile(canonicalPath);
		} catch (IOException e) {
			new IOError(file).throwSelfNative();
			return null;
		}
	}
	
	private static SourceFileInfo getOnlyFile(String canonicalPath) {
		SourceFileInfo srcInfo = sourceFiles.get(canonicalPath);
		if (srcInfo == null) {
			srcInfo = new SourceFileInfo(canonicalPath);
			sourceFiles.put(canonicalPath, srcInfo);
		}
		return srcInfo;
	}
	
	private String name;
	private boolean isImported;
	private boolean isRunning;
	private File file;
	
	private SourceFileInfo(String canonicalPath) {
		this.file = new File(canonicalPath);
		this.name = this.file.getName();
		this.isImported = false;
		this.isRunning = false;
	}
	
	public synchronized void markRunning() {
		this.isRunning = true;
	}
	
	public synchronized void unmarkRunning() {
		this.isRunning = false;
	}
	
	public synchronized void markImported() {
		this.isImported = true;
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
	
	public boolean isImported() {
		return this.isImported;
	}
	
	public File getFile() {
		return this.file;
	}
	
	public String getName() {
		return this.name;
	}
}
