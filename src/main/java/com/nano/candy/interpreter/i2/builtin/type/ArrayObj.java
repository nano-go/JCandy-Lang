package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.builtin.type.error.RangeError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ArrayHelper;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import com.nano.candy.utils.ArrayUtils;
import java.util.Arrays;

@NativeClass(name = "Array")
public final class ArrayObj extends BuiltinObject {
	
	public static final CandyClass ARRAY_CLASS = 
		NativeClassRegister.generateNativeClass(ArrayObj.class);
	
	public static final CandyObject[] EMPTY_ARRAY = new CandyObject[0];
	
	/**
	 * Requested maximum array size.
	 */
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	
	protected static void checkCapacity(long capacity) {
		if (capacity > Integer.MAX_VALUE || capacity < 0) {
			new ArgumentError("Illegal capacity: %d", capacity)
				.throwSelfNative();
		}
	}
	
	private CandyObject[] elements;
	private int size;
	
	private boolean isInProcessOfStr;
	
	public ArrayObj() {
		super(ARRAY_CLASS);
	}
	
	public ArrayObj(long n) {
		super(ARRAY_CLASS);
		initArray(n);
	}
	
	public ArrayObj(CandyObject[] elements) {
		super(ARRAY_CLASS);
		this.elements = elements;
		this.size = elements.length;
	}
	
	public ArrayObj(CandyObject[] elements, int size) {
		super(ARRAY_CLASS);
		this.elements = elements;
		this.size = size;
		if (size > elements.length) {
			new ArgumentError("Illegal size: %d", size)
				.throwSelfNative();
		}
	}
	
	private void initArray(long n) {
		if (n > MAX_ARRAY_SIZE || n < 0) {
			new ArgumentError("Illegal capacity: %d", n)
				.throwSelfNative();
		}
		if (n == 0) {
			elements = EMPTY_ARRAY;
		} else {
			elements = new CandyObject[(int)n];
		}
	}
	
	private void ensureCapacity(int minCapacity) {	
		if (elements == EMPTY_ARRAY) {
			elements = new CandyObject[Math.max(16, minCapacity)];
			return;
		}
		if (minCapacity - elements.length > 0) {
			if (minCapacity < 0) {
				new NativeError("Out of memory.").throwSelfNative();
			}
			int oldCapacity = elements.length;
			int newCapacity = oldCapacity + (oldCapacity >> 1);
			if (newCapacity < minCapacity)
				newCapacity = minCapacity;
			if (newCapacity > MAX_ARRAY_SIZE) {
				new NativeError("Out of memory.").throwSelfNative();
			}
			elements = Arrays.copyOf(elements, newCapacity);
		}
	}
	
	private int asIndex(CandyObject index) {
		long validIndex = ObjectHelper.asInteger(index);
		RangeError.checkIndex(validIndex, size);
		return (int) validIndex;
	}
	
	private int asIndexForInsert(CandyObject index) {
		long validIndex = ObjectHelper.asInteger(index);
		RangeError.checkIndex(validIndex, size + 1);
		return (int) validIndex;
	}
	
	public void append(CandyObject obj) {
		ensureCapacity(size + 1);
		elements[size ++] = obj;
	}
	
	public void addAll(CandyObject[] arr, int len) {
		addAll(this.size, arr, len);
	}
	
	public void addAll(int index, CandyObject[] arr, int len) {
		ensureCapacity(this.size + len);
		System.arraycopy(
			elements, index, elements, index + len, this.size-index);
		System.arraycopy(arr, 0, elements, index, len);
		this.size += len;
	}
	
	public CandyObject[] getBuiltinArray() {
		return elements;
	}
	
	public CandyObject get(int index) {
		RangeError.checkIndex(index, size);
		return elements[index];
	}
	
	public int size() {
		return size;
	}
	
	private int indexOf(VM vm, CandyObject obj) {
		for (int i = 0; i < size; i ++) {
			if (elements[i].equalsApiExeUser(vm, obj).value()) {
				return i;
			}
		}
		return -1;
	}
	
	private int lastIndexOf(VM vm, CandyObject obj) {
		for (int i = size-1; i >= 0; i --) {
			if (elements[i].equalsApiExeUser(vm, obj).value()) {
				return i;
			}
		}
		return -1;
	}
	
	private void insert(int index, CandyObject e) {
		if (index == size) {
			append(e);
			return;
		}
		ensureCapacity(size + 1);
		System.arraycopy(elements, index, elements, index + 1, size-index);
		elements[index] = e;
		size ++;
	}
	
	private boolean delete(VM vm, CandyObject obj) {
		int i = indexOf(vm, obj);
		if (i == -1) {
			return false;
		}
		deleteAt(i);
		return true;
	}
	
	public CandyObject deleteAt(int index) {
		CandyObject oldValue = elements[index];
		size--;
		if (index != size) {
			System.arraycopy(elements, index+1, elements, index, size-index);
		}
		elements[size] = null;
		return oldValue;
	}
	
	public void deleteRange(int from, int to) {
		if (from > to) {
			new RangeError("fromIndex(" + from + ") > toIndex(" + to + ")")
				.throwSelfNative();
		}
		if (to == from) {
			return;
		}
		int n = size - to;
		System.arraycopy(elements, to, elements, from, n);
		for (int i = from + n; i < size; i ++) {
			elements[i] = null;
		}
		size -= to-from;
	}
	
	public TupleObj toTuple() {
		return new TupleObj(Arrays.copyOf(elements, size));
	}
	
	@Override
	public CandyObject getItem(VM vm, CandyObject key) {
		return elements[asIndex(key)];
	}

	@Override
	public CandyObject setItem(VM vm, CandyObject key, CandyObject value) {
		elements[asIndex(key)] = value;
		return value;
	}

	@Override
	public CandyObject add(VM vm, CandyObject operand) {
		TypeError.checkTypeMatched(ARRAY_CLASS, operand);
		return new ArrayObj(
			ArrayUtils.mergeArray(elements, ((ArrayObj) operand).elements)
		);
	}
	
	@Override
	public CandyObject iterator(VM vm) {
		return new IteratorObj.ArrIterator(elements, size);
	}

	@Override
	public IntegerObj hashCode(VM vm) {
		int hash = 0;
		for (int i = 0; i < size; i ++) {
			hash = hash * 31 + 
				(int) elements[i].hashCodeApiExeUser(vm).intValue();
		}
		return IntegerObj.valueOf(hash);
	}

	@Override
	public BoolObj equals(VM vm, CandyObject operand) {
		if (this == operand) {
			return BoolObj.TRUE;
		}
		if (!(operand instanceof ArrayObj)) {
			return super.equals(vm, operand);
		}
		ArrayObj arr = (ArrayObj) operand;
		if (arr.size != size) {
			return BoolObj.FALSE;
		}
		final int SIZE = this.size;
		for (int i = 0; i < SIZE; i ++) {	
			BoolObj result = get(i).equalsApiExeUser(vm, arr.get(i));
			if (!result.value()) {
				return BoolObj.FALSE;
			}
		}
		return BoolObj.TRUE;
	}

	@Override
	public StringObj str(VM vm) {
		if (size == 0) {
			return StringObj.EMPTY_LIST;
		}
		if (isInProcessOfStr) {
			return StringObj.RECURSIVE_LIST;
		}
		isInProcessOfStr = true;
		StringBuilder builder = new StringBuilder("[");
		builder.append(ArrayHelper.toString(vm, elements, 0, size, ", "));
		builder.append("]");
		isInProcessOfStr = false;
		return StringObj.valueOf(builder.toString());
	}
	
	@NativeMethod(name = Names.METHOD_INITALIZER, argc = 2)
	public CandyObject initalizer(VM vm, CandyObject[] args) {
		long initalCapacity = ObjectHelper.asInteger(args[0]);
		CandyObject defaultElement = args[1];
		initArray(initalCapacity);
		
		if (defaultElement.isCallable()) {
			CallableObj callable = (CallableObj) defaultElement;
			for (int i = 0; i < initalCapacity; i ++) {
				CandyObject element = 
					callable.callExeUser(vm, IntegerObj.valueOf(i));
				append(element);
			}
		} else {
			for (int i = 0; i < initalCapacity; i ++) {
				append(defaultElement);
			}
		}	
		return this;
	}
	
	@NativeMethod(name = "append", argc = 1)
	public CandyObject append(VM vm, CandyObject[] args) {
		append(args[0]);
		return this;
	}
	
	@NativeMethod(name = "appendAll", argc = 1, varArgsIndex = 0)
	public CandyObject appendAll(VM vm, CandyObject[] args) {
		if (args[0] == NullPointer.nil()) {
			return this;
		}
		ArrayObj arr = (ArrayObj) args[0];
		addAll(arr.elements, arr.size);
		return this;
	}
	
	@NativeMethod(name = "insert", argc = 2)
	public CandyObject insert(VM vm, CandyObject[] args) {
		int index = asIndexForInsert(args[0]);
		insert(index, args[1]);
		return args[1];
	}
	
	@NativeMethod(name = "insertAll", argc = 2, varArgsIndex = 1)
	public CandyObject insertAllAt(VM vm, CandyObject[] args) {
		int index = asIndexForInsert(args[0]);
		if (args[1] == NullPointer.nil()) {
			return this;
		}
		ArrayObj arr = (ArrayObj) args[1];
		addAll(index, arr.elements, arr.size);
		return this;
	}
	
	@NativeMethod(name = "deleteAt", argc = 1)
	public CandyObject deleteAt(VM vm, CandyObject[] args) {
		return deleteAt(asIndex(args[0]));
	}
	
	@NativeMethod(name = "delete", argc = 1)
	public CandyObject delete(VM vm, CandyObject[] args) {
		return BoolObj.valueOf(delete(vm, args[0]));
	}
	
	@NativeMethod(name = "deleteRange", argc = 2)
	public CandyObject deleteRange(VM vm, CandyObject[] args) {
		deleteRange(asIndex(args[0]), asIndexForInsert(args[1]));
		return this;
	}
	
	@NativeMethod(name = "set", argc = 2)
	public CandyObject set(VM vm, CandyObject[] args) {
		return elements[asIndex(args[0])] = args[1];
	}
	
	@NativeMethod(name = "get", argc = 1)
	public CandyObject get(VM vm, CandyObject[] args) {
		return elements[asIndex(args[0])];
	}
	
	@NativeMethod(name = "contains", argc = 1)
	public CandyObject contains(VM vm, CandyObject[] args) {
		return BoolObj.valueOf(indexOf(vm, args[0]) != -1);
	}
	
	@NativeMethod(name = "indexOf", argc = 1)
	public CandyObject indexOf(VM vm, CandyObject[] args) {
		return IntegerObj.valueOf(indexOf(vm, args[0]));
	}
	
	@NativeMethod(name = "lastIndexOf", argc = 1)
	public CandyObject lastIndexOf(VM vm, CandyObject[] args) {
		return IntegerObj.valueOf(lastIndexOf(vm, args[0]));
	}
	
	@NativeMethod(name = "swap", argc = 2)
	public CandyObject swap(VM vm, CandyObject[] args) {
		int i = asIndex(args[0]);
		int j = asIndex(args[1]);
		
		CandyObject tmp = elements[i];
		elements[i] = elements[j];
		elements[j] = tmp;
		return this;
	}
	
	@NativeMethod(name = "copy")
	public CandyObject copy(VM vm, CandyObject[] args) {
		return new ArrayObj(Arrays.copyOf(elements, size));
	}
	
	@NativeMethod(name = "copyRange", argc = 2)
	public CandyObject copyRange(VM vm, CandyObject[] args) {
		int from = asIndex(args[0]);
		int to = asIndexForInsert(args[1]);
		return new ArrayObj(Arrays.copyOfRange(elements, from, to));
	}
	
	@NativeMethod(name = "sort") 
	public CandyObject sort(VM vm, CandyObject[] args) {
		Arrays.sort(elements, 0, size, ObjectHelper.newComparator(vm));
		return this;
	}
	
	@NativeMethod(name = "reverse")
	public CandyObject reverse(VM vm, CandyObject[] args) {
		int half = size/2;
		for (int i = 0; i < half; i ++) {
			CandyObject tmp = elements[i];
			elements[i] = elements[size-1-i];
			elements[size-1-i] = tmp;
		}
		return this;
	}
	
	@NativeMethod(name = "foreach", argc = 1)
	public CandyObject foreach(VM vm, CandyObject[] args) {
		CallableObj walker = TypeError.requiresCallable(args[0]);
		final int SIZE = size;
		for (int i = 0; i < SIZE; i ++) {
			walker.callExeUser(vm, IntegerObj.valueOf(i), elements[i]);
		}
		return this;
	}
	
	@NativeMethod(name = "toTuple")
	public CandyObject toTuple(VM vm, CandyObject[] args) {
		return toTuple();
	}
	
	@NativeMethod(name = "size")
	public CandyObject size(VM vm, CandyObject[] args) {
		return IntegerObj.valueOf(size);
	}
	
	@NativeMethod(name = "clear")
	public CandyObject clear(VM vm, CandyObject[] args) {
		for (int i = 0; i < size; i ++) {
			elements[i] = null;
		}
		size = 0;
		return this;
	}
}
