package com.nano.candy.interpreter.i1.builtin.type;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.classes.BooleanClass;

public class BooleanObject extends CandyObject {
	
    public static final BooleanObject TRUE = new BooleanObject(true) ;
	public static final BooleanObject FALSE = new BooleanObject(false) ;
	
	public static BooleanObject valueOf(boolean bool) {
		return bool ? TRUE : FALSE ;
	}
	
	public static BooleanObject valueOf(CandyObject obj) {
		if (obj == NullPointer.nil()) {
			return FALSE ;
		}
		if (obj instanceof BooleanObject) {
			return (BooleanObject) obj ;
		}
		return TRUE ;
	}
	
	private boolean value;
	private BooleanObject(boolean value) {
		super(BooleanClass.BOOL_CLASS);
		this.value = value;
		freeze();
	}

	public boolean value() {
		return value ;
	}
	
	public BooleanObject not() {
		return value ? FALSE : TRUE;
	}

	@Override
	public BooleanObject booleanValue() {
		return this ;
	}

	@Override
	public StringObject stringValue() {
		return StringObject.of(value ? "true" : "false") ;
	}
}
