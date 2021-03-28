package com.nano.candy.interpreter.i2.error;


import com.nano.candy.sys.CandySystem;
import java.io.File;
import java.io.IOException;

public class FileError extends CandyRuntimeError {
	
	/**
	 * Throws a <code>FileError</code> if the specified file is
	 * not Candy source file.
	 */
	public static void checkCandySourceFile(File file) {
		if (file.isFile()) {
			String name = file.getName();
			if (CandySystem.isCandySource(name)) {
				return;
			}
		}
		throw new FileError("This is not a source file: %s", file.getPath());
	}
	
	public FileError(File file) {
		super("Can't open the file: %s", file.getPath());
	}
	
	public FileError(IOException ioe) {
		super(ioe.getMessage());
	}
	
	public FileError(String errmsg, Object... args) {
		super(errmsg, args);
	}
	
	public FileError(String errmsg) {
		super(errmsg);
	}
}
