package com.nano.candy.interpreter.i2.vm;

import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import java.io.File;

/**
 * This denotes a compiled source file or a compiled chunk.
 */
public class CompiledFileInfo {

	private final String filepath;
	private final String simpleName;
	private final Chunk chunk;

	/**
	 * Whether this file is existed and is a source file.
	 */
	private final boolean isRealFile;

	public CompiledFileInfo(String filepath, Chunk chunk) {
		this(filepath, chunk, true);
	}

	public CompiledFileInfo(String filepath, Chunk chunk, boolean isRealFile) {
		this.filepath = filepath;
		this.simpleName = getSimpleName(filepath);
		this.isRealFile = isRealFile;
		this.chunk = chunk;
	}

	private static String getSimpleName(String filepath) {
		int index = filepath.lastIndexOf(File.separator);
		if (index > 0) {
			return filepath.substring(index + 1);
		}
		return filepath;
	}

	public boolean isRealFile() {
		return isRealFile;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public String getAbsPath() {
		return filepath;
	}

	public File getFile() {
		return new File(filepath);
	}

	public Chunk getChunk() {
		return chunk;
	}
}
