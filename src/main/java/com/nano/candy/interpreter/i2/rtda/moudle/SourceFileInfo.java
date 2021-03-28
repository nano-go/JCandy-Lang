package com.nano.candy.interpreter.i2.rtda.moudle;

import com.nano.candy.interpreter.i2.error.FileError;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SourceFileInfo {
	
	private static final HashMap<String, SourceFileInfo> sourceFiles = new HashMap<>();
	
	/**
	 * Returns the only source file info of the specified file.
	 *
	 * @return The source file info.
	 *
	 * @throws FileError
	 *         If an I/O error occurs or the file is not a Candy source file.
	 */
	public static SourceFileInfo get(File file) throws FileError {
		FileError.checkCandySourceFile(file);
		try {
			String canonicalPath = file.getCanonicalPath();
			return getOnlyFile(canonicalPath);
		} catch (IOException e) {
			throw new FileError(file);
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
