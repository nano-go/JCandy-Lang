package candyx.hash;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.CNativeObject;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;

@NativeClass(name = "HashMap", isInheritable = true)
public class HashMapObj extends CNativeObject {
	
	public final static CandyClass HASHMAP_CLASS = 
		NativeClassRegister.generateNativeClass(HashMapObj.class);

	private static final float LOAD_FACTOR = 0.75f;
	
	public static class Entry {
		private final int hash;
		private final CandyObject key;
		private CandyObject value;
		private Entry next;

		public Entry(CandyObject key, CandyObject value, int hash, Entry next) {
			this.key = key;
			this.value = value;
			this.hash = hash;
			this.next = next;
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
		
		public Entry getNext() {
			return next;
		}
		
		public Entry findEntry(VM vm, CandyObject key, int hash) {
			for (Entry entry = this; entry != null; entry = entry.next) {
				if (entry.equals(vm, key, hash)) {
					return entry;
				}
			}
			return null;
		}
		
		public boolean equals(VM vm, CandyObject key, int hash) {
			return this.hash == hash && (this.key == key ||
				this.key.equalsApiExeUser(vm, key).value());
		}
	}
		
	protected static int findPowerOfTwo(int capacity) {
		int c = 4;
		while (c < capacity) {
			c <<= 1;
		}
		return c;
	}

	protected static int hash(VM vm, CandyObject obj) {
		int h = (int) obj.hashCodeApiExeUser(vm).intValue();
		return (h >>> 16) ^ h;
	}
		
	private Entry[] table;
	private int size;
	private int threshoulds;
	
	protected HashMapObj() {
		super(HASHMAP_CLASS);
	}
	
	private void initCapacity(long capacity) {
		if (capacity > Integer.MAX_VALUE || capacity < 0) {
			new ArgumentError("Invalid capacity %d.", capacity)
				.throwSelfNative();
		}
		if (capacity >= 2) {
			capacity = 16;
		}
		int c = findPowerOfTwo((int)capacity);
		this.table = new Entry[c];
		threshoulds = (int) (c * LOAD_FACTOR);
	}
	
	private void checkCapacity() {
		if (size < threshoulds) {
			return;
		}
		resize(this.table.length*2);
	}

	private void resize(int newLength) {
		Entry[] oldTable = this.table;
		this.table = new Entry[newLength];
		this.threshoulds = (int) (newLength * LOAD_FACTOR);
		for (Entry e : oldTable) {
			while (e != null) {
				Entry n = e.next;
				fastPut(e);
				e = n;
			}
		}
	}

	private void fastPut(HashMapObj.Entry e) {
		int index = e.hash & (table.length - 1);
		e.next = table[index];
		table[index] = e;
	}
	
	protected CandyObject put(VM vm, CandyObject key, CandyObject value) {
		checkCapacity();
		int hash = hash(vm, key);
		int index = hash & (table.length - 1);
		Entry entry = table[index];
		if (entry == null ||
		    (entry = entry.findEntry(vm, key, hash)) == null) {
			table[index] = new Entry(key, value, hash, table[index]);
			size ++;
			return null;
		}
		CandyObject previousValue = entry.value;
		entry.value = value;
		return previousValue;
	}
	
	protected CandyObject get(VM vm, CandyObject key) {
		int hash = hash(vm, key);
		int index = hash & (table.length - 1);
		Entry entry = table[index];
		if (entry == null ||
		    (entry = entry.findEntry(vm, key, hash)) == null) {
			return null;
		}
		return entry.value;
	}
	
	protected CandyObject remove(VM vm, CandyObject key) {
		int hash = hash(vm, key);
		int index = hash & (table.length - 1);
		Entry p = null;
		Entry e = table[index];
		while (e != null) {
			if (!e.equals(vm, key, hash)) {
				p = e;
				e = e.next;
				continue;
			}
			if (p == null) {
				table[index] = e.next;
			} else {
				p.next = e.next;
				e.next = null;
			}
			size --;
			return e.value;
		}
		return null;
	}
	
	protected void clear() {
		size = 0;
		for (int i = 0; i < table.length; i ++) {
			table[i] = null;
		}
	}
	
	@NativeMethod(name = Names.METHOD_INITALIZER, argc = 1)
	public CandyObject init(VM vm, CandyObject[] args) {
		initCapacity(ObjectHelper.asInteger(args[0]));
		return this;
	}
	
	@NativeMethod(name = "put", argc = 2)
	public CandyObject put(VM vm, CandyObject[] args) {
		return put(vm, args[0], args[1]);
	}

	@Override
	public CandyObject setItem(VM vm, CandyObject key, CandyObject value) {
		return put(vm, key, value);
	}
	
	@NativeMethod(name = "get", argc = 1)
	public CandyObject get(VM vm, CandyObject[] args) {
		return get(vm, args[0]);
	}

	@Override
	public CandyObject getItem(VM vm, CandyObject key) {
		return get(vm, key);
	}
	
	@NativeMethod(name = "remove", argc = 1)
	public CandyObject remove(VM vm, CandyObject[] args) {
		return remove(vm, args[0]);
	}
	
	@NativeMethod(name = "size")
	public CandyObject size(VM vm, CandyObject[] args) {
		return IntegerObj.valueOf(size);
	}
	
	@NativeMethod(name = "clear")
	public CandyObject clear(VM vm, CandyObject[] args) {
		clear();
		return null;
	}
}
