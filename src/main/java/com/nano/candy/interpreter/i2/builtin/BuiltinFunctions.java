package com.nano.candy.interpreter.i2.builtin;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.MethodObj;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.Range;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.error.IOError;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.NativeContext;
import com.nano.candy.interpreter.i2.cni.NativeFunc;
import com.nano.candy.interpreter.i2.cni.NativeLibraryLoader;
import com.nano.candy.interpreter.i2.rtda.module.ModuleManager;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import com.nano.candy.sys.CandySystem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
	
	@NativeFunc(name = "methods", arity = 1) 
	public static CandyObject methods(VM vm, CandyObject[] args) {
		ArrayObj arr = new ArrayObj(16);
		for (CallableObj m : args[0].getCandyClass().getMethods()) {
			arr.append(new MethodObj(args[0], m));
		}
		return arr;
	}
	
	@NativeFunc(name = "bool", arity = 1) 
	public static CandyObject bool(VM vm, CandyObject[] args) {
		return args[0].boolValue(vm);
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
	
	@NativeFunc(name = "max", arity = 2)
	public static CandyObject max(VM vm, CandyObject[] args) {
		return args[0].gtApiExeUser(vm, args[1]).value()
			? args[0] : args[1];
	}
	
	@NativeFunc(name = "min", arity = 2)
	public static CandyObject min(VM vm, CandyObject[] args) {
		return args[0].ltApiExeUser(vm, args[1]).value()
			? args[0] : args[1];
	}
	
	@NativeFunc(name = "str", arity = 1)
	public static CandyObject str(VM vm, CandyObject[] args) {
		return args[0].strApiExeUser(vm);
	}

	@NativeFunc(name = "importModule", arity = 1)
	public static CandyObject importFile(VM vm, CandyObject[] args) {
		String filePath = ObjectHelper.asString(args[0]);
		return vm.getModuleManager().importModule(vm, filePath);
	}
	
	/**
	 * Adds the selected modules to the current file scope.
	 */
	@NativeFunc(name = "select", arity = 1)
	public static CandyObject select(VM vm, CandyObject[] args) {
		TypeError.checkTypeMatched(ArrayObj.ARRAY_CLASS, args[0]);
		ArrayObj files = (ArrayObj) args[0];
		final int size = files.size();
		final ModuleManager m = vm.getModuleManager();
		for (int i = 0; i < size; i ++) {
			ModuleObj moduleObj =
				m.importModule(vm, ObjectHelper.asString(files.get(i)));
			moduleObj.addToScope(vm.getGlobalScope());
		}
		return null;
	}
	
	/**
	 * Adds the modules selected by the specified filter to the current 
	 * file scope.
	 */
	@NativeFunc(name = "selectByFilter", arity = 1)
	public static CandyObject selectRegex(VM vm, CandyObject[] args) {
		TypeError.checkIsCallable(args[0]);
		CallableObj filter = (CallableObj) args[0];
		File currentDirectory = new File(vm.getCurrentDirectory());
		File[] subfiles = currentDirectory.listFiles();
		if (subfiles == null) {
			return null;
		}
		final ModuleManager m = vm.getModuleManager();
		ArrayList<StringObj> selectedFiles = new ArrayList<>();
		for (File f : subfiles) {
			// ignore the specific file.
			if (Names.MOUDLE_FILE_NAME.equals(f.getName())) {
				continue;
			}
			if (f.isFile() && !CandySystem.isCandySource(f.getName())) {
				continue;
			}
			CandyObject accept = ObjectHelper.callFunction
				(vm, filter, StringObj.valueOf(f.getName()));
			if (accept.boolValue(vm).value()) {
				ModuleObj moduleObj = m.importModule(vm, f.getName());
				moduleObj.addToScope(vm.getGlobalScope());
				selectedFiles.add(StringObj.valueOf(f.getName()));
			}
		}
		return new ArrayObj(selectedFiles.toArray(new StringObj[0]));
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
