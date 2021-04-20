package com.nano.candy.interpreter.i2.builtin;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.Range;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.error.IOError;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.NativeContext;
import com.nano.candy.interpreter.i2.cni.NativeFunc;
import com.nano.candy.interpreter.i2.cni.NativeLibraryLoader;
import com.nano.candy.interpreter.i2.vm.VM;
import java.io.IOException;

public class BuiltinFunctions {

	@NativeFunc(name = "print", arity = 1)
	public static CandyObject print(VM vm, CandyObject[] args) {
		System.out.print(ObjectHelper.callStr(vm, args[0]));
		return null;
	}

	@NativeFunc(name = "println", arity = 1)
	public static CandyObject println(VM vm, CandyObject[] args) {
		System.out.println(ObjectHelper.callStr(vm, args[0]));
		return null;
	}

	@NativeFunc(name = "range", arity = 2)
	public static CandyObject range(VM vm, CandyObject[] args) {
		return new Range(
			ObjectHelper.asInteger(args[0]),
			ObjectHelper.asInteger(args[1])
		);
	}
	
	@NativeFunc(name = "clock", arity = 0)
	public static CandyObject clock(VM vm, CandyObject[] args) {
		return IntegerObj.valueOf(System.currentTimeMillis());
	}

	@NativeFunc(name = "getAttr", arity = 2)
	public static CandyObject getAttr(VM vm, CandyObject[] args) {
		return args[0].getAttr(vm, ObjectHelper.asString(args[1]));
	}

	@NativeFunc(name = "setAttr", arity = 3)
	public static CandyObject setAttr(VM vm, CandyObject[] args) {
		CandyObject obj = args[0];
		obj.checkIsFrozen();
		String attrStr = ObjectHelper.asString(args[1]);
		obj.setAttr(vm, attrStr, args[2]);
		return args[2];
	}

	@NativeFunc(name = "str", arity = 1)
	public static CandyObject str(VM vm, CandyObject[] args) {
		return args[0].strApiExeUser(vm);
	}

	@NativeFunc(name = "importFile", arity = 1)
	public static CandyObject importFile(VM vm, CandyObject[] args) {
		String filePath = ObjectHelper.asString(args[0]);
		return vm.getMoudleManager().importFile(vm, filePath);
	}

	@NativeFunc(name = "cmdArgs", arity = 0)
	public static CandyObject cmd_args(VM vm, CandyObject[] args) {
		String[] cmdArgsStr = vm.getOptions().getArgs();
		CandyObject[] cmdArgs = new CandyObject[cmdArgsStr.length];
		for (int i = 0; i < cmdArgsStr.length; i ++) {
			cmdArgs[i] = StringObj.valueOf(cmdArgsStr[i]);
		}
		return new ArrayObj(cmdArgs);
	}

	@NativeFunc(name = "loadLibrary", arity = 2)
	public static CandyObject loadLibrary(VM vm, CandyObject[] args) {
		String path = ObjectHelper.asString(args[0]);
		String className = ObjectHelper.asString(args[1]);
		try {
			NativeContext context = NativeLibraryLoader
				.loadLibrary(vm.getFile(path), className);
			context.action(vm.getGlobalScope().curFileScope());
		} catch (IOException e) {
			new IOError(e).throwSelfNative();
		} catch (ClassNotFoundException e) {
			new NativeError("Class not found: " + e.getMessage())
				.throwSelfNative();
		}
		return null;
	}
}
