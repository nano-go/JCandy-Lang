package com.nano.candy.interpreter.i1.builtin.type;
import com.nano.candy.interpreter.error.CandyNullPointerError;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.classes.NullPointerClass;

public class NullPointer extends CandyObject {
	
	private static NullPointer nil = new NullPointer() ;
	
	public static NullPointer nil() {
		return nil ;
	}
	
	private NullPointer() {
		super(NullPointerClass.NULL_POINTER_CLASS);
		freeze();
	}

	@Override
	protected void throwFrozenObjError() {
		throw new CandyNullPointerError("");
	}

	@Override
	public BooleanObject booleanValue() {
		return BooleanObject.FALSE;
	}

	@Override
	public StringObject stringValue() {
		return StringObject.of(toString());
	}

	@Override
	public BooleanObject equalTo(CandyObject obj) {
		return BooleanObject.valueOf(obj == this);
	}
	
	@Override
	public String toString() {
		return "null";
	}
	
}

