package com.nano.candy.interpreter.i2.rtda.moudle;

import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import java.io.File;

/**
 * This denotes a compiled source file or a command line.
 */
public class CompiledFileInfo {

	private String filepath;
	private String simpleName;
	private boolean isRealFile;
	private Chunk chunk;

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
