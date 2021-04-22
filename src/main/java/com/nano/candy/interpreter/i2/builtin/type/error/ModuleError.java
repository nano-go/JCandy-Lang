package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;

@NativeClass(name = "ModuleError")
public class ModuleError extends ErrorObj {
	public static CandyClass MODULE_ERROR_CLASS =
		NativeClassRegister.generateNativeClass(ModuleError.class, ErrorObj.ERROR_CLASS);
	
	public ModuleError() {
		super(MODULE_ERROR_CLASS);
	}
	
	public ModuleError(String msg) {
		super(MODULE_ERROR_CLASS, msg);
	}
	
	public ModuleError(String msgFmt, Object... args) {
		super(MODULE_ERROR_CLASS, msgFmt, args);
	}
}
