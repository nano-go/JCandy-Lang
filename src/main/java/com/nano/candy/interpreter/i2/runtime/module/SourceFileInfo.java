package com.nano.candy.interpreter.i2.runtime.module;

import com.nano.candy.interpreter.i2.builtin.type.error.IOError;
import com.nano.candy.interpreter.i2.runtime.CandyThread;
import com.nano.candy.interpreter.i2.runtime.CarrierErrorException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SourceFileInfo {
	
	private static final HashMap<String, SourceFileInfo> sourceFiles = new HashMap<>();
	
	public static boolean markRunning(String fileName, CandyThread thread) {
		try {
			get(new File(fileName)).markRunning(thread);
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
			synchronized(SourceFileInfo.class) {
				String canonicalPath = file.getCanonicalPath();
				return getOnlyFile(canonicalPath);
			}
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
	private boolean isRunning;
	private File file;
	private CandyThread thread;
	
	private SourceFileInfo(String canonicalPath) {
		this.file = new File(canonicalPath);
		this.name = this.file.getName();
		this.isRunning = false;
	}
	
	public synchronized void markRunning(CandyThread thread) {
		this.isRunning = true;
		this.thread = thread;
	}
	
	public synchronized void unmarkRunning() {
		this.isRunning = false;
		this.thread = null;
	}
	
	public CandyThread getThread() {
		return this.thread;
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
	
	public File getFile() {
		return this.file;
	}
	
	public String getName() {
		return this.name;
	}
}
