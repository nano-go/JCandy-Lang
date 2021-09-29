package com.nano.candy.interpreter.builtin;

import com.nano.candy.interpreter.builtin.BuiltinFunctions;
import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.ObjectClass;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.BoolObj;
import com.nano.candy.interpreter.builtin.type.DoubleObj;
import com.nano.candy.interpreter.builtin.type.IntegerObj;
import com.nano.candy.interpreter.builtin.type.LockObj;
import com.nano.candy.interpreter.builtin.type.MapObj;
import com.nano.candy.interpreter.builtin.type.ModuleObj;
import com.nano.candy.interpreter.builtin.type.NumberObj;
import com.nano.candy.interpreter.builtin.type.Range;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.builtin.type.TupleObj;
import com.nano.candy.interpreter.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.builtin.type.error.AssertionError;
import com.nano.candy.interpreter.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.builtin.type.error.CompilerError;
import com.nano.candy.interpreter.builtin.type.error.ErrorObj;
import com.nano.candy.interpreter.builtin.type.error.IOError;
import com.nano.candy.interpreter.builtin.type.error.InterruptedError;
import com.nano.candy.interpreter.builtin.type.error.ModuleError;
import com.nano.candy.interpreter.builtin.type.error.NameError;
import com.nano.candy.interpreter.builtin.type.error.NativeError;
import com.nano.candy.interpreter.builtin.type.error.OverrideError;
import com.nano.candy.interpreter.builtin.type.error.RangeError;
import com.nano.candy.interpreter.builtin.type.error.StackOverflowError;
import com.nano.candy.interpreter.builtin.type.error.StackTraceElementObj;
import com.nano.candy.interpreter.builtin.type.error.StateError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.cni.JavaFunctionObj;
import com.nano.candy.interpreter.cni.NativeFuncRegister;
import com.nano.candy.interpreter.runtime.CandyThread;
import com.nano.candy.interpreter.runtime.Variable;
import java.util.HashMap;

public class BuiltinVariables {

	/**
	 * Read-only built-in variables.
	 */
	private static final HashMap<String, Variable> BUILTIN_VARS = 
		new HashMap<>();

	static {
		init();
	}

	private static void init() {
		defineBuiltinFunctions();
		defineBuiltinErrorClasses();
		defineClass(CandyThread.THREAD_CLASS);
		defineClass(LockObj.LOCK_CLASS);
		defineClass(Range.RANGE_CLASS);
		defineClass(ArrayObj.ARRAY_CLASS);
		defineClass(IntegerObj.INTEGER_CLASS);
		defineClass(NumberObj.NUMBER_CLASS);
		defineClass(DoubleObj.DOUBLE_CLASS);
		defineClass(StringObj.STRING_CLASS);
		defineClass(BoolObj.BOOL_CLASS);
		defineClass(TupleObj.TUPLE_CLASS);
		defineClass(ModuleObj.MOUDLE_CLASS);
		defineClass(MapObj.MAP_CLASS);
		defineClass(ObjectClass.getObjClass());
	}

	private static void defineBuiltinErrorClasses() {
		defineClass(ErrorObj.ERROR_CLASS);
		defineClass(ModuleError.MODULE_ERROR_CLASS);
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
		defineClass(StateError.STATE_ERROR_CLASS);
		defineClass(InterruptedError.INRERRUPTED_ERROR_CLASS);
		defineClass(OverrideError.OVERRIDE_ERROR_CLASS);

		defineClass(StackTraceElementObj.STACK_TRACE_ELEMENT_CLASS);
	}

	private static void defineBuiltinFunctions() {
		JavaFunctionObj[] builtinFunctions =
			NativeFuncRegister.getNativeFunctions(BuiltinFunctions.class);
		for (JavaFunctionObj f : builtinFunctions) {
			BUILTIN_VARS.put(f.funcName(), 
				Variable.getVariable(f.funcName(), f));
		}
	}

	private static void defineClass(CandyClass clazz) {
		BUILTIN_VARS.put(clazz.getName(),
			Variable.getVariable(clazz.getName(), clazz));
	}
	
	public static Variable getVariable(String name) {
		return BUILTIN_VARS.get(name);
	}
}
