package com.nano.candy.interpreter.i2.rtda;

import com.nano.candy.interpreter.i2.builtin.BuiltinFunctions;
import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.ObjectClass;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.MapObj;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.builtin.type.NumberObj;
import com.nano.candy.interpreter.i2.builtin.type.Range;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.TupleObj;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.i2.builtin.type.error.AssertionError;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.builtin.type.error.CompilerError;
import com.nano.candy.interpreter.i2.builtin.type.error.ErrorObj;
import com.nano.candy.interpreter.i2.builtin.type.error.IOError;
import com.nano.candy.interpreter.i2.builtin.type.error.ModuleError;
import com.nano.candy.interpreter.i2.builtin.type.error.NameError;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.builtin.type.error.RangeError;
import com.nano.candy.interpreter.i2.builtin.type.error.StackOverflowError;
import com.nano.candy.interpreter.i2.builtin.type.error.StackTraceElementObj;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.cni.CNativeFunction;
import com.nano.candy.interpreter.i2.cni.NativeFuncRegister;
import com.nano.candy.interpreter.i2.rtda.Variable;
import java.util.HashMap;

public class BuiltinVariables {

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

		defineClass(StackTraceElementObj.STACK_TRACE_ELEMENT_CLASS);
	}

	private static void defineBuiltinFunctions() {
		CNativeFunction[] builtinFunctions =
			NativeFuncRegister.getNativeFunctions(BuiltinFunctions.class);
		for (CNativeFunction f : builtinFunctions) {
			BUILTIN_VARS.put(f.declaredName(), 
				Variable.getVariable(f.declaredName(), f));
		}
	}

	private static void defineClass(CandyClass clazz) {
		BUILTIN_VARS.put(clazz.getName(),
			Variable.getVariable(clazz.getName(), clazz));
	}
	
	protected static HashMap<String, Variable> getVariables() {
		return BUILTIN_VARS;
	}
	
	public static Variable getVariable(String name) {
		return BUILTIN_VARS.get(name);
	}
}
