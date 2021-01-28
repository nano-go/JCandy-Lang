package com.nano.candy.interpreter.i1.env;
import com.nano.candy.interpreter.error.CandyNullPointerError;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.NullPointer;
import java.util.Objects;

public class Variable {
	private String name;
	private CandyObject reference;
	private Scope scope;

	public Variable(String name, Scope scope) {
		this(name, scope, null);
	}

	public Variable(String name, Scope scope, CandyObject reference) {
		this.name = name;
		this.scope = scope;
		this.reference = Objects.requireNonNullElse(reference, NullPointer.nil());
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}  
	
	public String getVariableName() {
		return name;
	}
	
	public Scope getScope() {
		return scope;
	}
	
	public boolean isNullPointer() {
		return reference == NullPointer.nil();
	}
	
	public void setReference(CandyObject ref) {
		this.reference = ref;
	}
	
	public <T extends CandyObject> T getReference() {
		return (T) reference;
	}
	
	public void throwNullPointerErrorIfNull() {
		if (isNullPointer()) {
			throw new CandyNullPointerError(this);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, reference);
	}
	
}
