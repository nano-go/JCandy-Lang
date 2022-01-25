package com.nano.candy.interpreter.builtin.type.error;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.type.error.IOError;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.sys.CandySystem;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;

@NativeClass(name = "IOError", isInheritable = true)
public class IOError extends ErrorObj {
	public static final CandyClass IO_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(IOError.class, ERROR_CLASS);
	/**
	 * Throws a <code>IOError</code> if the specified file is
	 * not a Candy source file.
	 */
	public static void checkCandySourceFile(File file) {
		if (file.isFile()) {
			String name = file.getName();
			if (CandySystem.isCandySource(name)) {
				return;
			}
		}
		new IOError("This is not a source file: %s", file.getPath())
			.throwSelfNative();
	}
	
	public static void checkFileExists(File file) {
		if (!file.exists()) {
			new IOError(file).throwSelfNative();
		}
	}
	
	public static String getExceptionMessage(Exception e) {
		if (e instanceof IOException) {
			return getIOExceptionMssage((IOException) e);
		}
		return e.getMessage();
	}

	private static String getIOExceptionMssage(IOException e) {
		if (e instanceof FileSystemException) { 
			String clsName = e.getClass().getSimpleName();
			if (clsName.endsWith("Exception")) {
				clsName = clsName.substring(
					0, clsName.length()-"Exception".length());
			}
			return e.getMessage() + " (" + clsName + ")";
		}
		return e.getMessage();
	}
	
	
	public IOError() {
		this("");
	}

	public IOError(File file) {
		super(IO_ERROR_CLASS, "Can't open the file: %s", file.getPath());
	}

	public IOError(Exception e) {
		super(IO_ERROR_CLASS, getExceptionMessage(e));
	}

	public IOError(String errmsg, Object... args) {
		super(IO_ERROR_CLASS, errmsg, args);
	}

	public IOError(String errmsg) {
		super(IO_ERROR_CLASS, errmsg);
	}
}
