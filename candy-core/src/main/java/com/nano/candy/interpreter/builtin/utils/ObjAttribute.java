package com.nano.candy.interpreter.builtin.utils;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.std.AttributeModifiers;

public class ObjAttribute {
	protected String name;
	protected CandyObject value;
	protected byte modifiers;
	
	public ObjAttribute(String name, CandyObject value, byte modifiers) {
		this.name = name;
		this.value = value;
		this.modifiers = modifiers;
	}

	public ObjAttribute(String name, CandyObject attr) {
		this.name = name;
		this.value = attr;
		this.modifiers = AttributeModifiers.PUBLIC;
	}

	public void setModifiers(byte modifiers) {
		this.modifiers = modifiers;
	}

	public byte getModifiers() {
		return modifiers;
	}
	
	public String getName() {
		return name;
	}
	
	public CandyObject getValue() {
		return value;
	}
	
	public void setValue(CandyObject value) {
		this.value = value; 
	}
	
	public boolean equals(String name) {
		return this.name.hashCode() == name.hashCode() &&
			this.name.equals(name);
	}
}
