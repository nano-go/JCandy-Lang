package com.nano.candy.interpreter.i1.builtin.func;

import com.nano.candy.interpreter.error.ExitError;
import com.nano.candy.interpreter.error.TypeError;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.classes.NumberClass;
import com.nano.candy.interpreter.i1.builtin.type.CallableObject;
import com.nano.candy.interpreter.i1.builtin.type.DoubleObject;
import com.nano.candy.interpreter.i1.builtin.type.IntegerObject;
import com.nano.candy.interpreter.i1.builtin.type.NullPointer;
import com.nano.candy.interpreter.i1.builtin.type.RangeObject;
import com.nano.candy.interpreter.i1.builtin.type.StringObject;

/**
 * Some built-in functions in this class.
 */
public abstract class BuiltinFunction extends CallableObject {

	public static final BuiltinFunction PRINT = new BuiltinFunction(1){
		@Override
		public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
			System.out.print(args[0].stringValue(interpreter).value());
			return NullPointer.nil();
		}
	};

	public static final BuiltinFunction PRINTLN = new BuiltinFunction(1){
		@Override
		public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
			System.out.println(args[0].stringValue(interpreter).value());
			return NullPointer.nil();
		}
	};

	public static final BuiltinFunction CLOCK = new BuiltinFunction(0){
		@Override
		public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
			return new DoubleObject(System.currentTimeMillis());
		}
	};

	public static final BuiltinFunction EXIT = new BuiltinFunction(1){
		@Override
		public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
			TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, args[0]);
			throw new ExitError((int)((IntegerObject)args[0]).intValue());
		}
	};
	
	public static final BuiltinFunction RANGE = new BuiltinFunction(2) {
		@Override
		public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
			TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, args[0]);
			TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, args[1]);
			long from = ((IntegerObject)args[0]).intValue();
			long to = ((IntegerObject)args[1]).intValue();
			return new RangeObject(from, to);
		}
	};

	public BuiltinFunction(int arity) {
		super(arity);
	}

	@Override
	public StringObject stringValue() {
		return StringObject.of("<built-in " + stringValue().value() + ">");
	}
}
