package com.nano.candy.interpreter.i2.rtda;

import com.nano.candy.interpreter.i2.builtin.BuiltinFunctions;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.MoudleObj;
import com.nano.candy.interpreter.i2.builtin.type.NumberObj;
import com.nano.candy.interpreter.i2.builtin.type.Range;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.TupleObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.ObjectClass;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.i2.builtin.type.error.AssertionError;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.builtin.type.error.CompilerError;
import com.nano.candy.interpreter.i2.builtin.type.error.ErrorObj;
import com.nano.candy.interpreter.i2.builtin.type.error.IOError;
import com.nano.candy.interpreter.i2.builtin.type.error.NameError;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.builtin.type.error.RangeError;
import com.nano.candy.interpreter.i2.builtin.type.error.StackOverflowError;
import com.nano.candy.interpreter.i2.builtin.type.error.StackTraceElementObj;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.cni.CNativeFunction;
import com.nano.candy.interpreter.i2.cni.NativeFuncRegister;
import com.nano.candy.interpreter.i2.rtda.moudle.CompiledFileInfo;
import com.nano.candy.interpreter.i2.rtda.moudle.SourceFileInfo;
import java.util.HashMap;

public class GlobalScope {
	
	private static final HashMap<String, Variable> BUILTIN_VARS = new HashMap<>();
	
	static {
		init();
	}
	
	private static void init() {
		defineBuiltinFunctions();
		defineBuiltinErrorClasses();
		defineClass(Range.RANGE_CLASS);
		defineClass(ArrayObj.ARRAY_CLASS);
		defineClass(IntegerObj.INTEGER_CLASS);
		defineClass(NumberObj.NUMBER_CLASS);
		defineClass(DoubleObj.DOUBLE_CLASS);
		defineClass(StringObj.STRING_CLASS);
		defineClass(BoolObj.BOOL_CLASS);
		defineClass(TupleObj.TUPLE_CLASS);
		defineClass(MoudleObj.MOUDLE_CLASS);
		defineClass(ObjectClass.getObjClass());
	}

	private static void defineBuiltinErrorClasses() {
		defineClass(ErrorObj.ERROR_CLASS);
		defineClass(ArgumentError.ARGUMENT_ERROR_CLASS);
		defineClass(AssertionError.ASSERTION_ERROR_CLASS);
		defineClass(AttributeError.ATTR_ERROR_CLASS);
		defineClass(CompilerError.COMPILER_ERROR_CLASS);
		defineClass(IOError.IO_ERROR_CLASS);
		defineClass(NameError.NAME_ERROR_CLASS);
		defineClass(NativeError.NATIVE_ERROR_CLASS);
		defineClass(RangeError.RANGE_ERROR_CLASS);
		defineClass(StackOverflowError.SOF_ERROR_CLASS);
		defineClass(TypeError.TYPE_ERROR_CLASS);
		
		defineClass(StackTraceElementObj.STACK_TRACE_ELEMENT_CLASS);
	}

	private static void defineBuiltinFunctions() {
		CNativeFunction[] builtinFunctions =
			NativeFuncRegister.getNativeFunctions(BuiltinFunctions.class);
		for (CNativeFunction f : builtinFunctions) {
			BUILTIN_VARS.put(f.declredName(), 
				Variable.getVariable(f.declredName(), f));
		}
	}

	private static void defineClass(CandyClass clazz) {
		BUILTIN_VARS.put(clazz.getClassName(),
			Variable.getVariable(clazz.getClassName(), clazz));
	}
	
	private HashMap<String, FileScope> fileScopePool;
	private FileScope curFileScope;
	
	public GlobalScope() {
		fileScopePool = new HashMap<>();
		curFileScope = null;
	}
	
	private FileScope getFileScope(CompiledFileInfo compiledFileInfo) {
		FileScope fs;
		String absPath;
		if (compiledFileInfo.isRealFile()) {
			absPath = SourceFileInfo.get(compiledFileInfo.getFile())
				.getFile().getAbsolutePath();
		} else {
			absPath = compiledFileInfo.getAbsPath();
		}
		fs = fileScopePool.get(absPath);
		if (fs == null) {
			fs = new FileScope(compiledFileInfo);
			fileScopePool.put(absPath, fs);
		}
		return fs;
	}
	
	public FileScope curFileScope() {
		return curFileScope;
	}
	
	public void setFileScope(FileScope fs) {
		curFileScope = fs;
	}
	
	public FileScope setFileScope(CompiledFileInfo compiledFileInfo) {
		curFileScope = getFileScope(compiledFileInfo);
		return curFileScope;
	}
	
	public void removeFileScope() {
		curFileScope = null;
	}
	
	public CandyObject getVar(String name) {
		CandyObject obj = curFileScope.getVar(name);
		if (obj != null) {
			return obj;
		}
		Variable variable = BUILTIN_VARS.get(name);
		if (variable != null) return variable.getValue();
		return null;
	}
	
	public Variable getVariable(String name) {
		Variable v = curFileScope.getVariable(name);
		if (v != null) {
			return v;
		}
		return BUILTIN_VARS.get(name);
	}
	
	public void setVar(String name, CandyObject value) {
		curFileScope.setVar(name, value);
	}
}
