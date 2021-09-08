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

	@NativeFunc(name = "print")
	public static CandyObject print(CNIEnv env, CandyObject arg) {
		env.getOptions().getStdout().print(arg.callStr(env).value());
		return null;
	}

	@NativeFunc(name = "println")
	public static CandyObject println(CNIEnv env, CandyObject arg) {
		env.getOptions().getStdout().println(arg.callStr(env).value());
		return null;
	}
	
	@NativeFunc(name = "readLine")
	public static CandyObject read(CNIEnv env) {
		try {
			return StringObj.valueOf(
				new Scanner(env.getOptions().getStdin()).nextLine());
		} catch (NoSuchElementException e) {
			new IOError("No line found").throwSelfNative();
		}
		return null;
	}

	@NativeFunc(name = "range")
	public static CandyObject range(CNIEnv env, long from, long to) {
		return new Range(from, to);
	}
	
	@NativeFunc(name = "sleep")
	public static CandyObject shelp(CNIEnv env, long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			new InterruptedError(e).throwSelfNative();
		}
		return null;
	}
	
	@NativeFunc(name = "curTime")
	public static CandyObject clock(CNIEnv env) {
		return IntegerObj.valueOf(System.currentTimeMillis());
	}
	
	@NativeFunc(name = "methods") 
	public static CandyObject methods(CNIEnv env, CandyObject arg) {
		ArrayObj arr = new ArrayObj(16);
		for (CallableObj m : arg.getCandyClass().getMethods()) {
			arr.append(new MethodObj(arg, m));
		}
		return arr;
	}

	@NativeFunc(name = "getAttr")
	public static CandyObject getAttr(CNIEnv env, CandyObject obj, String name) {
		return obj.getAttr(env, name);
	}

	@NativeFunc(name = "setAttr")
	public static CandyObject setAttr(CNIEnv env, CandyObject obj, String name, CandyObject value) {
		obj.checkFrozen();
		return obj.setAttr(env, name, value);
	}
	
	@NativeFunc(name = "max")
	public static CandyObject max(CNIEnv env, CandyObject x, CandyObject y) {
		return x.callGt(env, y).value() ? x : y;
	}
	
	@NativeFunc(name = "min")
	public static CandyObject min(CNIEnv env, CandyObject x, CandyObject y) {
		return x.callLt(env, y).value() ? x : y;
	}
	
	@NativeFunc(name = "bool") 
	public static CandyObject bool(CNIEnv env, CandyObject arg) {
		return arg.boolValue(env);
	}
	
	@NativeFunc(name = "str")
	public static CandyObject str(CNIEnv env, CandyObject arg) {
		return arg.callStr(env);
	}
	
	@NativeFunc(name = "array")
	public static CandyObject array(CNIEnv env, CandyObject arg) {
		if (arg instanceof ArrayObj) {
			return (ArrayObj) arg;
		}
		if (arg instanceof TupleObj) {
			return ((TupleObj) arg).toArrayObj();
		}
		return new ArrayObj(ObjectHelper.iterableObjToArray(env, arg));
	}
	
	@NativeFunc(name = "tuple")
	public static CandyObject tuple(CNIEnv env, CandyObject arg) {
		if (arg instanceof TupleObj) {
			return (TupleObj) arg;
		}
		if (arg instanceof ArrayObj) {
			return ((ArrayObj) arg).toTuple();
		}
		return new TupleObj(ObjectHelper.iterableObjToArray(env, arg));
	}
	
	@NativeFunc(name = "repeat")
	public static CandyObject repear(CNIEnv env, long count, CallableObj fn) {
		for (int i = 0; i < count; i ++) {
			ObjectHelper.callFunction(env, fn);
		}
		return null;
	}

	@NativeFunc(name = "importModule")
	public static CandyObject importFile(CNIEnv env, String filePath) {
		return ModuleManager.getManager().importModule(env, filePath);
	}
	
	/**
	 * Adds the selected modules to the current file scope.
	 */
	@NativeFunc(name = "select")
	public static CandyObject select(CNIEnv env, ArrayObj files) {
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
	@NativeFunc(name = "selectByFilter")
	public static CandyObject selectByFilter(CNIEnv env, CallableObj filter) {
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

	@NativeFunc(name = "cmdArgs")
	public static CandyObject cmd_args(CNIEnv env) {
		String[] cmdArgsStr = env.getOptions().getArgs();
		CandyObject[] cmdArgs = new CandyObject[cmdArgsStr.length];
		for (int i = 0; i < cmdArgsStr.length; i ++) {
			cmdArgs[i] = StringObj.valueOf(cmdArgsStr[i]);
		}
		return new ArrayObj(cmdArgs);
	}

	@NativeFunc(name = "loadLibrary")
	public static CandyObject loadLibrary(CNIEnv env, String path, String className) {
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
	
	@NativeFunc(name = "exit")
	public static CandyObject exit(CNIEnv env, int exitCode) {
		System.exit(exitCode);
		return null;
	}
}
