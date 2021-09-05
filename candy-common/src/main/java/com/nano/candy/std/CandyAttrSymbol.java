package com.nano.candy.std;

public class CandyAttrSymbol {
	
	private byte modifiers;
	private String name;

	public CandyAttrSymbol(byte modifiers, String name) {
		this.modifiers = modifiers;
		this.name = name;
	}

	public void setModifiers(byte modifiers) {
		this.modifiers = modifiers;
	}

	public byte getModifiers() {
		return modifiers;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;	
		if (obj instanceof CandyAttrSymbol) {
			return (name).equals(((CandyAttrSymbol) obj).name);
		}
		return false;
	}
}
