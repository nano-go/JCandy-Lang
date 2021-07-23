package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.MapObj;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;

@NativeClass(name = "Map", isInheritable=true)
public final class MapObj extends CandyObject {

	public static final CandyClass MAP_CLASS = 
		NativeClassRegister.generateNativeClass(MapObj.class);
	
	private static final int DEFAULT_INIT_CAPACITY = 16;
	
	private static final int MAXIMUM_CAPACITY = 1 << 30;
	
	private static final float LOAD_FACTOR = 0.75f;
	
	public static class Entry {
		final int hash;
		final CandyObject key;
		CandyObject value;
		Entry next;

		public Entry(CandyObject key, CandyObject value, int hash) {
			this.key = key;
			this.value = value;
			this.hash = hash;
		}

		public CandyObject getKey() {
			return key;
		}
		
		public CandyObject getValue() {
			return value;
		}

		public int getHash() {
			return hash;
		}
		
		protected Entry findByKey(VM vm, CandyObject key, int hash) {
			Entry entry = this;
			do {
				if (entry.equals(vm, key, hash)) {
					return entry;
				}
				entry = entry.next;
			} while (entry != null);
			return null;
		}
		
		protected boolean equals(VM vm, CandyObject key, int hash) {
			return this.hash == hash &&
				this.key.callEquals(vm, key).value();
		}
	}
	
	private static int hash(VM vm, CandyObject obj) {
		int hash = (int) obj.callHashCode(vm).value;
		return (hash >> 16) ^ hash;
	}
	
	static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
	
	private Entry[] table;
	private int size;
	private int initCapacity;
	private int threadshould;
	
	protected MapObj() {
		super(MAP_CLASS);
		this.initCapacity = DEFAULT_INIT_CAPACITY;
	}
	
	public MapObj(long initCapacity) {
		super(MAP_CLASS);
		setInitCapacity(initCapacity);
	}

	private void setInitCapacity(long initCapacity) {
		if (initCapacity < 0 || initCapacity > MAXIMUM_CAPACITY) {
			new ArgumentError("Illegal inital capacity: " + initCapacity)
				.throwSelfNative();
		}
		this.initCapacity = tableSizeFor((int) initCapacity);
		this.threadshould = (int) (this.initCapacity*LOAD_FACTOR);
	}
	
	private void ensureTableSize() {
		if (table == null) {
			table = new Entry[initCapacity];
		}
		if (this.size >= threadshould) {
			resize();
		}
	}

	private void resize() {
		int newSize = table.length*2;
		this.threadshould = (int) (newSize*LOAD_FACTOR);
		MapObj.Entry[] oldTable = this.table;
		this.table = new MapObj.Entry[newSize];
		for (MapObj.Entry entry : oldTable) {
			putEntry(entry);
		}
	}

	private void putEntry(MapObj.Entry entry) {
		Entry e = entry;
		while (e != null) {
			Entry n = e.next;
			int index = e.hash & (this.table.length-1);
			if (table[index] != null) {
				e.next = table[index];
			} else e.next = null;
			table[index] = e;
			e = n;
		}
	}
	
	public CandyObject put(VM vm, CandyObject key, CandyObject value) {
		if (value == null) {
			value = NullPointer.nil();
		}
		ensureTableSize();
		int hash = hash(vm, key);
		int index = hash & (this.table.length-1);
		Entry entry;
		if (table[index] != null && 
			(entry = table[index].findByKey(vm, key, hash)) != null) {
			CandyObject previousValue = entry.value;
			entry.value = value;
			return previousValue;
		}
		Entry newEntry = new Entry(key, value, hash);
		newEntry.next = table[index];
		table[index] = newEntry;
		this.size ++;
		return null;
	}
	
	public CandyObject putIfAbsent(VM vm, CandyObject key, CandyObject value) {
		if (value == null) {
			value = NullPointer.nil();
		}
		ensureTableSize();
		int hash = hash(vm, key);
		int index = hash & (this.table.length-1);
		Entry entry;
		if (table[index] != null && 
			(entry = table[index].findByKey(vm, key, hash)) != null) {
			return entry.value;
		}
		Entry newEntry = new Entry(key, value, hash);
		newEntry.next = table[index];
		table[index] = newEntry;
		this.size ++;
		return null;
	}
	
	public CandyObject putAll(VM vm, MapObj map) {
		EntryIterator i = new EntryIterator(map.table);
		while (i.hasNext(vm)) {
			put(vm, i.currentEntry.key, i.currentEntry.value);
			i.next(vm);
		}
		return this;
	}
	
	public CandyObject get(VM vm, CandyObject key) {
		return getOrDefault(vm, key, null);
	}
	
	public CandyObject getOrDefault(VM vm, CandyObject key, CandyObject def) {
		if (table == null) return null;
		Entry entry;
		int hash = hash(vm, key);
		int index = hash & (this.table.length-1);
		if (table[index] != null && 
			(entry = table[index].findByKey(vm, key, hash)) != null) {
			return entry.value;
		}
		return def;
	}
	
	public boolean contains(VM vm, CandyObject key) {
		return get(vm, key) != null;
	}
	
	public CandyObject remove(VM vm, CandyObject key) {
		if (table == null) return null;
		int hash = hash(vm, key);
		int index = hash & (this.table.length-1);
		Entry previous = null;
		Entry entry = table[index];
		while (entry != null) {
			if (entry.equals(vm, key, hash)) {
				if (previous == null) {
					table[index] = null;
				} else {
					previous.next = entry.next;
				}
				size --;
				break;
			}
			previous = entry;
			entry = entry.next;
		}
		return entry != null ? entry.value : null;
	}
	
	public void clear() {
		if (table != null && size != 0) {
			size = 0;
			for (int i = 0; i < table.length; i ++)
				table[i] = null;
		}
	}
	
	@NativeMethod(name = Names.METHOD_INITALIZER, argc = 1)
	public CandyObject init(VM vm, CandyObject[] args) {
		setInitCapacity(ObjectHelper.asInteger(args[0]));
		return this;
	}
	
	@NativeMethod(name = "get", argc = 1)
	public CandyObject get(VM vm, CandyObject[] args) {
		return get(vm, args[0]);
	}
	
	@NativeMethod(name = "getOrDefault", argc = 2)
	public CandyObject getOrDefault(VM vm, CandyObject[] args) {
		return getOrDefault(vm, args[0], args[1]);
	}

	@Override
	public CandyObject getItem(VM vm, CandyObject key) {
		return get(vm, key);
	}
	
	@NativeMethod(name = "put", argc = 2)
	public CandyObject put(VM vm, CandyObject[] args) {
		return put(vm, args[0], args[1]);
	}
	
	@NativeMethod(name = "putIfAbsent", argc = 2)
	public CandyObject putIfAbsent(VM vm, CandyObject[] args) {
		return putIfAbsent(vm, args[0], args[1]);
	}
	
	@NativeMethod(name = "putAll", argc = 1) 
	public CandyObject putAll(VM vm, CandyObject[] args) {
		TypeError.checkTypeMatched(MAP_CLASS, args[0]);
		return putAll(vm, (MapObj) args[0]);
	}
	
	@Override
	public CandyObject setItem(VM vm, CandyObject key, CandyObject value) {
		put(vm, key, value);
		return value;
	}
	
	@NativeMethod(name = "remove", argc = 1)
	public CandyObject remove(VM vm, CandyObject[] args) {
		return remove(vm, args[0]);
	}
	
	@NativeMethod(name = "contains", argc = 1)
	public CandyObject contains(VM vm, CandyObject[] args) {
		return BoolObj.valueOf(contains(vm, args[0]));
	}
	
	@NativeMethod(name = "length", argc = 0)
	public CandyObject size(VM vm, CandyObject[] args) {
		return IntegerObj.valueOf(size);
	}
	
	@NativeMethod(name = "isEmpty", argc = 0)
	public CandyObject isEmpty(VM vm, CandyObject[] args) {
		return BoolObj.valueOf(size == 0);
	}
	
	@NativeMethod(name = "clear", argc = 0)
	public CandyObject clear(VM vm, CandyObject[] args) {
		clear();
		return null;
	}

	@Override
	public StringObj str(VM vm) {
		EntryIterator i = new EntryIterator(this.table);
		if (!i.hasNext(vm)) {
			return StringObj.valueOf("{}");
		}
		StringBuilder builder = new StringBuilder("{");
		while (true) {
			String key = i.currentEntry.key.callStr(vm).value();
			String value = i.currentEntry.value.callStr(vm).value();
			builder.append(key).append(": ").append(value);
			i.moveToNext();
			if (!i.hasNext(vm)) {
				return StringObj.valueOf(builder.append("}").toString());
			}
			builder.append(", ");
		}
	}

	@Override
	public BoolObj equals(VM vm, CandyObject operand) {
		if (this == operand) {
			return BoolObj.TRUE;
		}
		if (operand instanceof MapObj) {
			MapObj map = (MapObj) operand;
			if (map.size != this.size) {
				return BoolObj.FALSE;
			}
			EntryIterator i = new EntryIterator(this.table);
			while (i.hasNext(vm)) {
				CandyObject value = map.get(vm, i.currentEntry.key);
				if (value == null ||
					 !value.callEquals(vm, i.currentEntry.value).value()) {
					return BoolObj.FALSE;
				}
				i.moveToNext();
			}
		}
		return BoolObj.TRUE;
	}

	@Override
	public CandyObject iterator(VM vm) {
		return new EntryIterator(this.table);
	}
	
	private static class EntryIterator extends IteratorObj {
		
		private static final Entry[] EMPTY_ENTRY = new Entry[0];
		private Entry[] table;
		private Entry currentEntry;
		private int index;

		public EntryIterator(Entry[] table) {
			this.table = table == null ? EMPTY_ENTRY : table;
			moveToNext();
		}

		private void moveToNext() {
			if (currentEntry != null) {
				currentEntry = currentEntry.next;
				if (currentEntry != null) {
					return;
				}
			}
			while (index < table.length) {
				if (table[index] != null) {
					currentEntry = table[index ++];
					break;
				}
				index ++;
			}
		}
		
		@Override
		public boolean hasNext(VM vm) {
			return currentEntry != null;
		}

		@Override
		public CandyObject next(VM vm) {
			TupleObj tupleObj = new TupleObj(new CandyObject[]{
				currentEntry.key, currentEntry.value
			});
			moveToNext();
			return tupleObj;
		}	
	}
}
