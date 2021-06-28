package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@NativeClass(name = "NativeError", isInheritable = true)
public class NativeError extends ErrorObj {
	public static final CandyClass NATIVE_ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(NativeError.class, ERROR_CLASS);
	
	private static String nativeErrorMessage(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		String msg = String.format(
			"A java exception occurs.\n    %s: %s\n    java stack:%s", 
			throwable.getClass().getSimpleName(), 
			throwable.getMessage(),
			sw.toString()
		);
		try {
			pw.close();
			sw.close();
		} catch (IOException e) {}
		return msg;
	}
		
	public NativeError() {
		super(NATIVE_ERROR_CLASS);
	}
	
	public NativeError(String msg) {
		super(NATIVE_ERROR_CLASS, msg);
	}
	
	public NativeError(Throwable throwable) {
		this(nativeErrorMessage(throwable));
	}
}
