package com.nano.candy.interpreter.builtin.utils;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.std.AttributeModifiers;
import com.nano.candy.std.CandyAttrSymbol;
import java.util.Set;

public abstract class SymbolTable {
	
	private static final SymbolTable EMPTY = new EmptySymbolTable();
	
	public static SymbolTable empty() {
		return EMPTY;
	}
	
	protected abstract void ensureEnoughTableSize(int minimumSize);
	
	public abstract CandyObject put(String name, CandyObject value);
	public abstract CandyObject putWithModfiers(String name, CandyObject value, byte modifiers);
	public abstract CandyObject remove(String name);
	public abstract CandyObject get(String name);
	public abstract ObjAttribute getAttr(String name);
	public abstract int size();
	
	public final void putAll(Set<CandyAttrSymbol> attrs) {
		ensureEnoughTableSize(size() + attrs.size());
		for (CandyAttrSymbol symbol : attrs) {
			putWithModfiers(
				symbol.getName(), NullPointer.nil(), symbol.getModifiers()); 
		}
	}
	
	public final boolean isEmpty() {
		return size() == 0;
	}
	
	public CandyObject putReadOnlyAttr(String name, CandyObject value) {
		return putWithModfiers(name, value, AttributeModifiers.READ_ONLY);
	}
	
	public CandyObject putWriteOnlyAttr(String name, CandyObject value) {
		return putWithModfiers(name, value, AttributeModifiers.WRITE_ONLY);
	}
	
}
