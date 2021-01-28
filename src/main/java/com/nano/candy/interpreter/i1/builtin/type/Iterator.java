package com.nano.candy.interpreter.i1.builtin.type;
import com.nano.candy.interpreter.error.ArgumentError;
import com.nano.candy.interpreter.error.AttributeError;
import com.nano.candy.interpreter.error.TypeError;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyHelper;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.func.BuiltinMethodHelper;

/**
 * A object is iterator if it has a (_next()) and a (_hasNext()) 
 * attributes or methods.
 *
 * _next(): Returns the next element.
 * _hasNext(): Returns {@code true} if the iterator has more elements.
 */
public class Iterator {

	private Callable hasNext;
	private Callable next;

	public Iterator(CandyObject obj) {
		CandyObject hasNext = obj.getAttr("_hasNext");
		CandyObject next = obj.getAttr("_next");
		this.hasNext = checkIsValidIteratorMethod(obj, "_hasNext", hasNext);
		this.next = checkIsValidIteratorMethod(obj, "_next", next);
	}

	private Callable checkIsValidIteratorMethod(CandyObject iterator, String name, CandyObject method) {
		AttributeError.requiresAttrNonNull("Iterator", name, method);
		Callable callable = TypeError.checkCallable(method, name);
		ArgumentError.checkArguments(
			callable.arity(), 0, "'" + name + "'"
		);
		return callable;
	}

	public CandyObject next(AstInterpreter interpreter) {
		return next.onCall(interpreter, CandyHelper.EMPTY_ARGUMENT);
	}

	public boolean hasNext(AstInterpreter interpreter) {
		return hasNext.onCall(interpreter, CandyHelper.EMPTY_ARGUMENT)
			.booleanValue(interpreter).value();
	}

	public static abstract class IteratorObject extends CandyObject {

		public IteratorObject() {
			setAttr("_hasNext", generateHasNextMethod());
			setAttr("_next", generateNextMethod());
			freeze();
		}

		protected CandyObject generateHasNextMethod() {
			return BuiltinMethodHelper.generateMethod(this, 0, new BuiltinMethodHelper.Callback(){
				@Override
				public CandyObject onCall(AstInterpreter i, CandyObject instance, CandyObject[] args) {
					return BooleanObject.valueOf(((IteratorObject)instance).hasNext());
				}
			});
		}

		protected CandyObject generateNextMethod() {
			return BuiltinMethodHelper.generateMethod(this, 0, new BuiltinMethodHelper.Callback(){
				@Override
				public CandyObject onCall(AstInterpreter i, CandyObject instance, CandyObject[] args) {
					return ((IteratorObject)instance).next();
				}
			});
		}

		public abstract boolean hasNext();
		public abstract CandyObject next();
	}
}
