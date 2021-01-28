package com.nano.candy.interpreter.i1.builtin.func;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.Callable;
import com.nano.candy.interpreter.i1.builtin.type.CallableObject;

public class BuiltinMethodHelper {
	
	public static final CallableObject EMPTY_INTIALIZER = new BuiltinMethod(0, new Callback() {
		@Override
		public CandyObject onCall(AstInterpreter interpreter, CandyObject instance, CandyObject[] args) {
			return instance;
		}
	});
	
	public static CallableObject generateMethod(CandyObject instance, int arity, Callback callback) {
		return new BuiltinMethod(instance, arity, callback);
	}
	
	public static CallableObject generateMethod(int arity, Callback callback) {
		return new BuiltinMethod(arity, callback);
	}
	
	public static interface Callback {
		public CandyObject onCall(AstInterpreter i, CandyObject instance, CandyObject[] args);
	}    
	
	private static class BuiltinMethod extends CallableObject {

		private Callback callback;
		private CandyObject instance;
		
		public BuiltinMethod(CandyObject instance, int arity, Callback callback) {
			this(arity, callback);
			this.instance = instance;
		}
		
		public BuiltinMethod(int arity, Callback callback) {
			super(arity);
			this.callback = callback;
		}

		@Override
		public Callable bindToInstance(CandyObject instance) {
			return new BuiltinMethod(instance, arity, callback);
		}
		
		@Override
		public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
			return callback.onCall(interpreter, instance, args);
		}
	}
}
