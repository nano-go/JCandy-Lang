package com.nano.candy.interpreter.i1.builtin.type;
import com.nano.candy.interpreter.error.CandyRuntimeError;
import com.nano.candy.interpreter.error.TypeError;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.classes.BuiltinClassImpl;
import com.nano.candy.interpreter.i1.builtin.classes.NumberClass;
import com.nano.candy.interpreter.i1.builtin.classes.annotation.BuiltinClass;
import com.nano.candy.interpreter.i1.builtin.func.annotation.BuiltinMethod;

@BuiltinClass("Range")
public class RangeObject extends CandyObject {
	
	public static BuiltinClassImpl RANGE_CLASS = BuiltinClassImpl.newBuiltinClass(RangeObject.class);
	
	private static class RangeIterator extends Iterator.IteratorObject {
		private int v;
		private long left, right;

		public RangeIterator(long left, long right) {
			this.left = left;
			this.right = right;
			this.v = left > right ? -1 : 1;
		}
		
		@Override
		public boolean hasNext() {
			return left != right;
		}

		@Override
		public CandyObject next() {
			IntegerObject next = new IntegerObject(left);
			left += v;
			return next;
		}
	}
	
	long left, right;
	
	// for reflection.
	public RangeObject() {
		super(RANGE_CLASS);
	}

	public RangeObject(long left, long right) {
		super(RANGE_CLASS);
		this.left = left;
		this.right = right;
	}
	
	public long getLeft() {
		return left;
	}
	
	public long getRight() {
		return right;
	}

	@Override
	public CandyObject iterator() {
		return new RangeIterator(left, right);
	}

	@Override
	public StringObject stringValue() {
		return StringObject.of(String.format("[%d, %d)", left, right));
	}

	@Override
	public CandyObject setAttr(String attr, CandyObject ref) {
		if ("left".equals(attr)) {
			TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, ref);
			this.left = ((IntegerObject) ref).intValue();
		} else if ("right".equals(attr)) {
			TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, ref);
			this.right = ((IntegerObject) ref).intValue();
		}
		return super.setAttr(attr, ref);
	}
	
	/*===================== Built-in Methods ===================*/
	
	@BuiltinMethod(value="", argc=2)
	public CandyObject initializer(AstInterpreter interpreter, CandyObject[] args) {
		TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, args[0]);
		TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, args[1]);
		long left = ((IntegerObject) args[0]).intValue();
		long right = ((IntegerObject) args[1]).intValue();
		this.left = left;
		this.right = right;
		setAttr("left", args[0]);
		setAttr("right", args[1]);
		return this;
	}
	
	@BuiltinMethod("rand")
	public CandyObject rand(AstInterpreter interpreter, CandyObject[] args) {
		if (left == right) {
			throw new CandyRuntimeError("Empty set: %s", stringValue().value());
		}
		return new IntegerObject(left + (int) (Math.random() * (right - left))) ;
	}
	
	@BuiltinMethod("toArray")
	public CandyObject toArray(AstInterpreter interpreter, CandyObject[] args) {
		ArrayObject array = new ArrayObject();
		int v = left > right ? -1 : 1;
		for (long i = left; i != right; i += v) {
			array.append(new IntegerObject(i));
		}
		return array;
	}
}
