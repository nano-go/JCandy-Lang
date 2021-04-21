package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.sys.CandySystem;
import java.io.File;
import java.io.IOException;

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
	
	public IOError() {
		this("");
	}

	public IOError(File file) {
		super(IO_ERROR_CLASS, "Can't open the file: %s", file.getPath());
	}

	public IOError(IOException ioe) {
		super(IO_ERROR_CLASS, ioe.getMessage());
	}

	public IOError(String errmsg, Object... args) {
		super(IO_ERROR_CLASS, errmsg, args);
	}

	public IOError(String errmsg) {
		super(IO_ERROR_CLASS, errmsg);
	}
}
