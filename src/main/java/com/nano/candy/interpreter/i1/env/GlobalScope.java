package com.nano.candy.interpreter.i1.env;
import com.nano.candy.interpreter.i1.builtin.classes.BooleanClass;
import com.nano.candy.interpreter.i1.builtin.classes.BuiltinClassImpl;
import com.nano.candy.interpreter.i1.builtin.classes.CallableClass;
import com.nano.candy.interpreter.i1.builtin.classes.NullPointerClass;
import com.nano.candy.interpreter.i1.builtin.classes.NumberClass;
import com.nano.candy.interpreter.i1.builtin.classes.ObjectClass;
import com.nano.candy.interpreter.i1.builtin.classes.StringClass;
import com.nano.candy.interpreter.i1.builtin.func.BuiltinFunction;
import com.nano.candy.interpreter.i1.builtin.type.ArrayObject;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;
import com.nano.candy.interpreter.i1.builtin.type.RangeObject;

public class GlobalScope extends CommonScope{
	
	public GlobalScope() {
		super(null);
		init();
	}

	private void init() {
		defineVariable("print", BuiltinFunction.PRINT);
		defineVariable("println", BuiltinFunction.PRINTLN);
		defineVariable("clock", BuiltinFunction.CLOCK);
		defineVariable("exit", BuiltinFunction.EXIT);
		defineVariable("range", BuiltinFunction.RANGE);
		
		defineClass(ObjectClass.getInstance());
		defineClass(CallableClass.getInstance());
		defineClass(NullPointerClass.NULL_POINTER_CLASS);
		defineClass(BooleanClass.BOOL_CLASS);
		defineClass(NumberClass.NUMBER_CLASS);
		defineClass(NumberClass.DOUBLE_CLASS);
		defineClass(NumberClass.INTEGER_CLASS);
		defineClass(StringClass.STRING_CLASS);
		defineClass(ArrayObject.ARRAY_CLASS);
		
		defineClass(RangeObject.RANGE_CLASS);
	}
	
	private void defineClass(CandyClass clazz) {
		defineVariable(clazz.getClassName(), clazz);
		if (clazz instanceof BuiltinClassImpl) {
			((BuiltinClassImpl)clazz).defineMethods();
		}
	}
	
}
