package com.nano.candy.interpreter.i2.rtda;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.functions.BuiltinFunctionEntity;
import com.nano.candy.interpreter.i2.builtin.functions.BuiltinFunctions;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.NumberObj;
import com.nano.candy.interpreter.i2.builtin.type.Range;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.TupleObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.ObjectClass;
import com.nano.candy.interpreter.i2.rtda.moudle.CompiledFileInfo;
import com.nano.candy.interpreter.i2.rtda.moudle.SourceFileInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class GlobalEnvironment {
	
	private static final HashMap<String, CandyObject> BUILTIN_VARS = new HashMap<>();
	
	static {
		init();
	}
	
	private static void init() {
		defineBuiltinFunctions();
		defineClass(Range.RANGE_CLASS);
		defineClass(ArrayObj.ARRAY_CLASS);
		defineClass(IntegerObj.INTEGER_CLASS);
		defineClass(NumberObj.NUMBER_CLASS);
		defineClass(DoubleObj.DOUBLE_CLASS);
		defineClass(StringObj.STRING_CLASS);
		defineClass(BoolObj.BOOL_CLASS);
		defineClass(TupleObj.TUPLE_CLASS);
		defineClass(ObjectClass.getObjClass());
	}

	private static void defineBuiltinFunctions() {
		for (Field field : BuiltinFunctions.class.getFields()) {
			int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers)) {
				continue;
			}
			if (field.getType() == BuiltinFunctionEntity.class) {
				try {
					defineBuiltinFunction(
						(BuiltinFunctionEntity) field.get(null));
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		}
	}

	private static void defineBuiltinFunction(BuiltinFunctionEntity func) {
		BUILTIN_VARS.put(func.declredName(), func);
	}

	private static void defineClass(CandyClass clazz) {
		BUILTIN_VARS.put(clazz.getClassName(), clazz);
	}
	
	private HashMap<String, FileScope> fileScopePool;
	private FileScope curFileScope;
	
	public GlobalEnvironment() {
		fileScopePool = new HashMap<>();
		curFileScope = null;
	}
	
	public FileScope getFileScope(CompiledFileInfo compiledFileInfo) {
		FileScope fs;
		String onlyPath;
		if (compiledFileInfo.isRealFile()) {
			onlyPath = SourceFileInfo.get(compiledFileInfo.getFile())
				.getFile().getAbsolutePath();
		} else {
			onlyPath = compiledFileInfo.getAbsPath();
		}
		fs = fileScopePool.get(onlyPath);
		if (fs == null) {
			fs = new FileScope(compiledFileInfo);
			fileScopePool.put(onlyPath, fs);
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
	
	public void clearFileScope() {
		curFileScope = null;
	}
	
	public CandyObject getVar(String name) {
		CandyObject obj = curFileScope.getVar(name);
		if (obj != null) {
			return obj;
		}
		return BUILTIN_VARS.get(name);
	}
	
	public void setVar(String name, CandyObject value) {
		curFileScope.setVar(name, value);
	}
	
}
