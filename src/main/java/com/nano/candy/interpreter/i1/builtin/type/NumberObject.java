package com.nano.candy.interpreter.i1.builtin.type;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.func.annotation.BuiltinMethod;

public abstract class NumberObject extends CandyObject {

	public NumberObject(CandyClass clazz) {
		super(clazz);
		freeze();
	}
	
	public abstract long intValue();
	public abstract double doubleValue();
	public abstract boolean isUpperCastClass();

	@Override
	public CandyObject plus(AstInterpreter interpreter, CandyObject obj) {
		if (!(obj instanceof NumberObject)) {
			return super.plus(interpreter, obj);
		}
		NumberObject n = (NumberObject)obj;
		if (isUpperCastClass() || n.isUpperCastClass()) {
			return new DoubleObject(doubleValue() + n.doubleValue());
		}
		return new IntegerObject(intValue() + n.intValue());
	}

	@Override
	public CandyObject subtract(CandyObject obj) {
		if (!(obj instanceof NumberObject)) {
			return super.subtract(obj);
		}
		NumberObject n = (NumberObject)obj;
		if (isUpperCastClass() || n.isUpperCastClass()) {
			return new DoubleObject(doubleValue() - n.doubleValue());
		}
		return new IntegerObject(intValue() - n.intValue());
	}

	@Override
	public CandyObject times(CandyObject obj) {
		if (!(obj instanceof NumberObject)) {
			return super.times(obj);
		}
		NumberObject n = (NumberObject)obj;
		if (isUpperCastClass() || n.isUpperCastClass()) {
			return new DoubleObject(doubleValue() * n.doubleValue());
		}
		return new IntegerObject(intValue() * n.intValue());
	}

	@Override
	public CandyObject divide(CandyObject obj) {
		if (!(obj instanceof NumberObject)) {
			return super.divide(obj);
		}
		NumberObject n = (NumberObject)obj;
		if (isUpperCastClass() || n.isUpperCastClass()) {
			return new DoubleObject(doubleValue() / n.doubleValue());
		}
		return new IntegerObject(intValue() / n.intValue());
	}

	@Override
	public CandyObject mod(CandyObject obj) {
		if (!(obj instanceof NumberObject)) {
			return super.mod(obj);
		}
		NumberObject n = (NumberObject)obj;
		if (isUpperCastClass() || n.isUpperCastClass()) {
			return new DoubleObject(doubleValue() % n.doubleValue());
		}
		return new IntegerObject(intValue() % n.intValue());
	}

	@Override
	public BooleanObject greaterThan(CandyObject obj) {
		if (!(obj instanceof NumberObject)) {
			return super.greaterThan(obj);
		}
		return BooleanObject.valueOf(
			doubleValue() > ((NumberObject)obj).doubleValue()
		);
	}

	@Override
	public BooleanObject greaterThanOrEqualTo(CandyObject obj) {
		if (!(obj instanceof NumberObject)) {
			return super.greaterThanOrEqualTo(obj);
		}
		return BooleanObject.valueOf(
			doubleValue() >= ((NumberObject)obj).doubleValue()
		);
	}

	@Override
	public BooleanObject lessThan(CandyObject obj) {
		if (!(obj instanceof NumberObject)) {
			return super.lessThan(obj);
		}
		return BooleanObject.valueOf(
			doubleValue() < ((NumberObject)obj).doubleValue()
		);
	}

	@Override
	public BooleanObject lessThanOrEqualTo(CandyObject obj) {
		if (!(obj instanceof NumberObject)) {
			return super.lessThanOrEqualTo(obj);
		}
		return BooleanObject.valueOf(
			doubleValue() <= ((NumberObject)obj).doubleValue()
		);
	}

	@Override
	public BooleanObject equalTo(CandyObject obj) {
		if (!(obj instanceof NumberObject)) {
			return super.equalTo(obj);
		}
		return BooleanObject.valueOf(
			doubleValue() == ((NumberObject)obj).doubleValue()
		);
	}

	@Override
	public StringObject stringValue() {
		return StringObject.of(String.valueOf(doubleValue()));
	}
	
	/*===================== Built-in Methods ===================*/
	
	@BuiltinMethod("intVal")
	public CandyObject intVal(AstInterpreter interpreter, CandyObject[] args) {
		return new IntegerObject(intValue());
	}

	@BuiltinMethod("doubleVal")
	public CandyObject doubleVal(AstInterpreter interpreter, CandyObject[] args) {
		return new DoubleObject(intValue());
	}
}
