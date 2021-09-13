package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.MapObj;
import com.nano.candy.interpreter.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeMethod;
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
		
		protected Entry findByKey(CNIEnv env, CandyObject key, int hash) {
			Entry entry = this;
			do {
				if (entry.equals(env, key, hash)) {
					return entry;
				}
				entry = entry.next;
			} while (entry != null);
			return null;
		}
		
		protected boolean equals(CNIEnv env, CandyObject key, int hash) {
			return this.hash == hash &&
				this.key.callEquals(env, key).value();
		}
	}
	
	private static int hash(CNIEnv env, CandyObject obj) {
		int hash = (int) obj.callHashCode(env).value;
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
	
	public CandyObject put(CNIEnv env, CandyObject key, CandyObject value) {
		if (value == null) {
			value = NullPointer.nil();
		}
		ensureTableSize();
		int hash = hash(env, key);
		int index = hash & (this.table.length-1);
		Entry entry;
		if (table[index] != null && 
			(entry = table[index].findByKey(env, key, hash)) != null) {
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
	
	public CandyObject putIfAbsent(CNIEnv env, CandyObject key, CandyObject value) {
		if (value == null) {
			value = NullPointer.nil();
		}
		ensureTableSize();
		int hash = hash(env, key);
		int index = hash & (this.table.length-1);
		Entry entry;
		if (table[index] != null && 
			(entry = table[index].findByKey(env, key, hash)) != null) {
			return entry.value;
		}
		Entry newEntry = new Entry(key, value, hash);
		newEntry.next = table[index];
		table[index] = newEntry;
		this.size ++;
		return null;
	}
	
	public CandyObject putAll(CNIEnv env, MapObj map) {
		EntryIterator i = new EntryIterator(map.table);
		while (i.hasNext(env)) {
			put(env, i.currentEntry.key, i.currentEntry.value);
			i.next(env);
		}
		return this;
	}
	
	public CandyObject get(CNIEnv env, CandyObject key) {
		return getOrDefault(env, key, null);
	}
	
	public CandyObject getOrDefault(CNIEnv env, CandyObject key, CandyObject def) {
		if (table == null) return null;
		Entry entry;
		int hash = hash(env, key);
		int index = hash & (this.table.length-1);
		if (table[index] != null && 
			(entry = table[index].findByKey(env, key, hash)) != null) {
			return entry.value;
		}
		return def;
	}
	
	public boolean contains(CNIEnv env, CandyObject key) {
		return get(env, key) != null;
	}
	
	public CandyObject remove(CNIEnv env, CandyObject key) {
		if (table == null) return null;
		int hash = hash(env, key);
		int index = hash & (this.table.length-1);
		Entry previous = null;
		Entry entry = table[index];
		while (entry != null) {
			if (entry.equals(env, key, hash)) {
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
	
	@Override
	protected final CandyObject getItem(CNIEnv env, CandyObject key) {
		return ObjectHelper.preventNull(get(env, key));
	}

	@Override
	protected final CandyObject setItem(CNIEnv env, CandyObject key, CandyObject value) {
		put(env, key, value);
		return value;
	}
	
	@NativeMethod(name = Names.METHOD_INITALIZER)
	protected final CandyObject init(CNIEnv env, long initialCapacity) {
		setInitCapacity(initialCapacity);
		return this;
	}
	
	@NativeMethod(name = "get")
	protected final CandyObject getMet(CNIEnv env, CandyObject key) {
		return get(env, key);
	}
	
	@NativeMethod(name = "getOrDefault")
	protected final CandyObject getOrDefaultMet(CNIEnv env, CandyObject key, CandyObject defaultValue) {
		return getOrDefault(env, key, defaultValue);
	}
	
	@NativeMethod(name = "put")
	protected final CandyObject putMet(CNIEnv env, CandyObject key, CandyObject value) {
		return put(env, key, value);
	}
	
	@NativeMethod(name = "putIfAbsent")
	protected final CandyObject putIfAbsentMet(CNIEnv env, CandyObject key, CandyObject value) {
		return putIfAbsent(env, key, value);
	}
	
	@NativeMethod(name = "putAll") 
	protected final CandyObject putAll(CNIEnv env, CandyObject map) {
		TypeError.checkTypeMatched(MAP_CLASS, map);
		return putAll(env, (MapObj) map);
	}
	
	@NativeMethod(name = "remove")
	protected final CandyObject removeMet(CNIEnv env, CandyObject key) {
		return remove(env, key);
	}
	
	@NativeMethod(name = "contains")
	protected final CandyObject containsMet(CNIEnv env, CandyObject key) {
		return BoolObj.valueOf(contains(env, key));
	}
	
	@NativeMethod(name = "length")
	protected final CandyObject size(CNIEnv env) {
		return IntegerObj.valueOf(size);
	}
	
	@NativeMethod(name = "isEmpty")
	protected final CandyObject isEmpty(CNIEnv env) {
		return BoolObj.valueOf(size == 0);
	}
	
	@NativeMethod(name = "clear")
	protected final CandyObject clear(CNIEnv env) {
		clear();
		return null;
	}

	@Override
	public StringObj str(CNIEnv env) {
		EntryIterator i = new EntryIterator(this.table);
		if (!i.hasNext(env)) {
			return StringObj.valueOf("{}");
		}
		StringBuilder builder = new StringBuilder("{");
		while (true) {
			String key = i.currentEntry.key.callStr(env).value();
			String value = i.currentEntry.value.callStr(env).value();
			builder.append(key).append(": ").append(value);
			i.moveToNext();
			if (!i.hasNext(env)) {
				return StringObj.valueOf(builder.append("}").toString());
			}
			builder.append(", ");
		}
	}

	@Override
	public BoolObj equals(CNIEnv env, CandyObject operand) {
		if (this == operand) {
			return BoolObj.TRUE;
		}
		if (operand instanceof MapObj) {
			MapObj map = (MapObj) operand;
			if (map.size != this.size) {
				return BoolObj.FALSE;
			}
			EntryIterator i = new EntryIterator(this.table);
			while (i.hasNext(env)) {
				CandyObject value = map.get(env, i.currentEntry.key);
				if (value == null ||
					 !value.callEquals(env, i.currentEntry.value).value()) {
					return BoolObj.FALSE;
				}
				i.moveToNext();
			}
		}
		return BoolObj.TRUE;
	}

	@Override
	public CandyObject iterator(CNIEnv env) {
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
		public boolean hasNext(CNIEnv env) {
			return currentEntry != null;
		}

		@Override
		public CandyObject next(CNIEnv env) {
			TupleObj tupleObj = new TupleObj(new CandyObject[]{
				currentEntry.key, currentEntry.value
			});
			moveToNext();
			return tupleObj;
		}	
	}
}
