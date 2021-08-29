package com.nano.candy.interpreter.builtin.type.error;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.type.error.ErrorObj;
import com.nano.candy.interpreter.builtin.type.error.ModuleError;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;

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
