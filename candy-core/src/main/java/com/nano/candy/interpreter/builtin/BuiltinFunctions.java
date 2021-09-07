package com.nano.candy.interpreter.builtin;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.IntegerObj;
import com.nano.candy.interpreter.builtin.type.MethodObj;
import com.nano.candy.interpreter.builtin.type.ModuleObj;
import com.nano.candy.interpreter.builtin.type.Range;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.builtin.type.TupleObj;
import com.nano.candy.interpreter.builtin.type.error.IOError;
import com.nano.candy.interpreter.builtin.type.error.InterruptedError;
import com.nano.candy.interpreter.builtin.type.error.NativeError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeContext;
import com.nano.candy.interpreter.cni.NativeFunc;
import com.nano.candy.interpreter.cni.NativeLibraryLoader;
import com.nano.candy.interpreter.runtime.module.ModuleManager;
import com.nano.candy.std.Names;
import com.nano.candy.sys.CandySystem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class BuiltinFunctions {

	@NativeFunc(name = "print", arity = 1)
	public static CandyObject print(CNIEnv env, CandyObject[] args) {
		System.out.print(args[0].callStr(env).value());
		return null;
	}

	@NativeFunc(name = "println", arity = 1)
	public static CandyObject println(CNIEnv env, CandyObject[] args) {
		System.out.println(args[0].callStr(env).value());
		return null;
	}
	
	@NativeFunc(name = "readLine")
	public static CandyObject read(CNIEnv env, CandyObject[] args) {
		try {
			return StringObj.valueOf(new Scanner(System.in).nextLine());
		} catch (NoSuchElementException e) {
			new IOError("No line found").throwSelfNative();
		}
		return null;
	}

	@NativeFunc(name = "range", arity = 2)
	public static CandyObject range(CNIEnv env, CandyObject[] args) {
		return new Range(
			ObjectHelper.asInteger(args[0]),
			ObjectHelper.asInteger(args[1])
		);
	}
	
	@NativeFunc(name = "sleep", arity = 1)
	public static CandyObject shelp(CNIEnv env, CandyObject[] args) {
		try {
			Thread.sleep(ObjectHelper.asInteger(args[0]));
		} catch (InterruptedException e) {
			new InterruptedError(e).throwSelfNative();
		}
		return null;
	}
	
	@NativeFunc(name = "curTime", arity = 0)
	public static CandyObject clock(CNIEnv env, CandyObject[] args) {
		return IntegerObj.valueOf(System.currentTimeMillis());
	}
	
	@NativeFunc(name = "methods", arity = 1) 
	public static CandyObject methods(CNIEnv env, CandyObject[] args) {
		ArrayObj arr = new ArrayObj(16);
		for (CallableObj m : args[0].getCandyClass().getMethods()) {
			arr.append(new MethodObj(args[0], m));
		}
		return arr;
	}

	@NativeFunc(name = "getAttr", arity = 2)
	public static CandyObject getAttr(CNIEnv env, CandyObject[] args) {
		return args[0].getAttr(env, ObjectHelper.asString(args[1]));
	}

	@NativeFunc(name = "setAttr", arity = 3)
	public static CandyObject setAttr(CNIEnv env, CandyObject[] args) {
		CandyObject obj = args[0];
		String attrStr = ObjectHelper.asString(args[1]);
		obj.checkFrozen();
		obj.setAttr(env, attrStr, args[2]);
		return args[2];
	}
	
	@NativeFunc(name = "max", arity = 2)
	public static CandyObject max(CNIEnv env, CandyObject[] args) {
		return args[0].callGt(env, args[1]).value()
			? args[0] : args[1];
	}
	
	@NativeFunc(name = "min", arity = 2)
	public static CandyObject min(CNIEnv env, CandyObject[] args) {
		return args[0].callLt(env, args[1]).value()
			? args[0] : args[1];
	}
	
	@NativeFunc(name = "bool", arity = 1) 
	public static CandyObject bool(CNIEnv env, CandyObject[] args) {
		return args[0].boolValue(env);
	}
	
	@NativeFunc(name = "str", arity = 1)
	public static CandyObject str(CNIEnv env, CandyObject[] args) {
		return args[0].callStr(env);
	}
	
	@NativeFunc(name = "array", arity = 1)
	public static CandyObject array(CNIEnv env, CandyObject[] args) {
		if (args[0] instanceof ArrayObj) {
			return (ArrayObj) args[0];
		}
		if (args[0] instanceof TupleObj) {
			return ((TupleObj) args[0]).toArrayObj();
		}
		return new ArrayObj(ObjectHelper.iterableObjToArray(env, args[0]));
	}
	
	@NativeFunc(name = "tuple", arity = 1)
	public static CandyObject tuple(CNIEnv env, CandyObject[] args) {
		if (args[0] instanceof TupleObj) {
			return (TupleObj) args[0];
		}
		if (args[0] instanceof ArrayObj) {
			return ((ArrayObj) args[0]).toTuple();
		}
		return new TupleObj(ObjectHelper.iterableObjToArray(env, args[0]));
	}
	
	@NativeFunc(name = "repeat", arity = 2)
	public static CandyObject repear(CNIEnv env, CandyObject[] args) {
		final long COUNT = ObjectHelper.asInteger(args[0]);
		CallableObj fn = TypeError.requiresCallable(args[1]);
		for (int i = 0; i < COUNT; i ++) {
			ObjectHelper.callFunction(env, fn);
		}
		return null;
	}

	@NativeFunc(name = "importModule", arity = 1)
	public static CandyObject importFile(CNIEnv env, CandyObject[] args) {
		String filePath = ObjectHelper.asString(args[0]);
		return ModuleManager.getManager().importModule(env, filePath);
	}
	
	/**
	 * Adds the selected modules to the current file scope.
	 */
	@NativeFunc(name = "select", arity = 1)
	public static CandyObject select(CNIEnv env, CandyObject[] args) {
		TypeError.checkTypeMatched(ArrayObj.ARRAY_CLASS, args[0]);
		ArrayObj files = (ArrayObj) args[0];
		final int size = files.length();
		final ModuleManager m = ModuleManager.getManager();
		for (int i = 0; i < size; i ++) {
			ModuleObj moduleObj =
				m.importModule(env, ObjectHelper.asString(files.get(i)));
			moduleObj.addToEnv(env.getCurrentFileEnv());
		}
		return null;
	}
	
	/**
	 * Adds the modules selected by the specified filter to the current 
	 * file scope.
	 */
	@NativeFunc(name = "selectByFilter", arity = 1)
	public static CandyObject selectByFilter(CNIEnv env, CandyObject[] args) {
		TypeError.checkIsCallable(args[0]);
		CallableObj filter = (CallableObj) args[0];
		File currentDirectory = new File(env.getCurrentDirectory());
		File[] subfiles = currentDirectory.listFiles();
		if (subfiles == null) {
			return null;
		}
		final ModuleManager m = ModuleManager.getManager();
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
				(env, filter, StringObj.valueOf(f.getName()));
			if (accept.boolValue(env).value()) {
				ModuleObj moduleObj = m.importModule(env, f.getName());
				moduleObj.addToEnv(env.getCurrentFileEnv());
				selectedFiles.add(StringObj.valueOf(f.getName()));
			}
		}
		return new ArrayObj(selectedFiles.toArray(new StringObj[0]));
	}

	@NativeFunc(name = "cmdArgs", arity = 0)
	public static CandyObject cmd_args(CNIEnv env, CandyObject[] args) {
		String[] cmdArgsStr = env.getOptions().getArgs();
		CandyObject[] cmdArgs = new CandyObject[cmdArgsStr.length];
		for (int i = 0; i < cmdArgsStr.length; i ++) {
			cmdArgs[i] = StringObj.valueOf(cmdArgsStr[i]);
		}
		return new ArrayObj(cmdArgs);
	}

	@NativeFunc(name = "loadLibrary", arity = 2)
	public static CandyObject loadLibrary(CNIEnv env, CandyObject[] args) {
		String path = ObjectHelper.asString(args[0]);
		String className = ObjectHelper.asString(args[1]);
		try {
			NativeContext context = NativeLibraryLoader
				.loadLibrary(env.getJavaLibraryPaths(), path, className);
			context.action(env);
		} catch (IOException e) {
			new IOError(e).throwSelfNative();
		} catch (ClassNotFoundException e) {
			new NativeError("Class not found: " + e.getMessage())
				.throwSelfNative();
		}
		return null;
	}
	
	@NativeFunc(name = "exit", arity=1)
	public static CandyObject exit(CNIEnv env, CandyObject[] args) {
		System.exit((int)ObjectHelper.asInteger(args[0]));
		return null;
	}
}
