package com.nano.candy.interpreter.builtin.utils;
import com.nano.candy.interpreter.builtin.CandyObject;

public class HashSymbolTable extends SymbolTable {

	private static float LOAD_FACTOR = 0.5f;
	
	private static int tableSizeFor(int capacity) {
		int size = 1;
		while (size < capacity) {
			size <<= 1;
		}
		return size;
	}

	private static final int hash(Object obj) {
		int hash = obj.hashCode();
		return (hash >> 16) ^ hash;
	}
	
	private ObjAttribute table[];
	private int threshold;
	private int size;
	
	public HashSymbolTable() {
		this(8);
	}
	
	public HashSymbolTable(int initialCapacity) {
		this.table = null;
		this.threshold = tableSizeFor(initialCapacity);
		this.size = 0;
	}
	
	final boolean isActive(ObjAttribute attr) {
		return attr != null && attr.name != null;
	}
	
	final void resize() {
		ObjAttribute[] oldTable = this.table;
		this.table = new ObjAttribute[oldTable.length*2];
		this.threshold = (int) (this.table.length * LOAD_FACTOR);
		putOldTable(oldTable);
	}

	final void putOldTable(ObjAttribute[] t) {
		final int length = table.length;
		for (ObjAttribute e : t) {
			if (e == null || e.name == null) {
				continue;
			}
			int index = hash(e.getName()) & length-1;
			int offset = 1;
			while (table[index] != null) {
				index = (index + offset) % length;
				offset += 2;
			}
			table[index] = e;
		}
	}
	
	final int findPos(String name) {
		final int length = table.length;
		int index = hash(name) & length-1;
		int offset = 1;
		while (isActive(table[index]) && !table[index].equals(name)) {
			index = (index + offset) % length;
			offset += 2;
		}
		return index;
	}

	@Override
	protected void ensureEnoughTableSize(int minimumSize) {
		if (this.table == null || minimumSize >= threshold) {
			ObjAttribute[] oldTable = this.table;
			this.table = new ObjAttribute[tableSizeFor(minimumSize)];
			this.threshold = (int) (this.table.length * LOAD_FACTOR);
			if (oldTable != null)
				putOldTable(oldTable);
		}
	}
	
	@Override
	public CandyObject put(String name, CandyObject value) {
		if (this.table == null) {
			this.table = new ObjAttribute[
				threshold = threshold < 8 ? 8 : threshold];
			this.threshold *= LOAD_FACTOR;
		}
		int index = findPos(name);
		ObjAttribute attr = this.table[index];
		if (attr != null) {
			CandyObject old = attr.value;
			attr.value = value;
			if (attr.name != null) {
				return old;
			}
			attr.name = name;	
		} else {
			this.table[index] = new ObjAttribute(name, value);
		}
		this.size ++;
		if (size >= threshold) {
			resize();
		}
		return null;
	}

	@Override
	public CandyObject putWithModfiers(String name, CandyObject value, byte modifiers) {
		if (this.table == null) {
			this.table = new ObjAttribute[
				threshold = threshold < 8 ? 8 : threshold];
			this.threshold *= LOAD_FACTOR;
		}
		int index = findPos(name);
		ObjAttribute attr = this.table[index];
		if (attr != null) {
			CandyObject old = attr.value;
			attr.value = value;
			attr.modifiers = modifiers;
			if (attr.name != null) {
				return old;
			}
			attr.name = name;	
		} else {
			this.table[index] = new ObjAttribute(name, value, modifiers);
		}
		this.size ++;
		if (size >= threshold) {
			resize();
		}
		return null;
	}

	@Override
	public CandyObject remove(String name) {
		if (size == 0) { 
			return null;
		}
		ObjAttribute attr = this.table[findPos(name)];
		if (isActive(attr)) {
			CandyObject old = attr.value;
			attr.name = null;
			attr.value = null;
			size --;
			return old;
		}
		return null;
	}

	@Override
	public CandyObject get(String name) {
		if (size == 0) { 
			return null;
		}
		ObjAttribute attr = this.table[findPos(name)];
		return attr != null ? attr.value : null;
	}

	@Override
	public ObjAttribute getAttr(String name) {
		if (size == 0) { 
			return null;
		}
		ObjAttribute attr = this.table[findPos(name)];
		return isActive(attr) ? attr : null;
	}

	@Override
	public int size() {
		return size;
	}
    
}
