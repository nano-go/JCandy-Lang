package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.builtin.type.error.RangeError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ArrayHelper;
import com.nano.candy.interpreter.i2.builtin.utils.IndexHelper;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.std.Names;
import com.nano.candy.utils.ArrayUtils;
import java.util.Arrays;
import java.util.Random;

@NativeClass(name = "Array", isInheritable=true)
public final class ArrayObj extends CandyObject {
	
	public static final CandyClass ARRAY_CLASS = 
		NativeClassRegister.generateNativeClass(ArrayObj.class);
	
	public static final CandyObject[] EMPTY_ARRAY = new CandyObject[0];
	
	public static final ArrayObj emptyArray() {
		return new ArrayObj(EMPTY_ARRAY); 
	}
	
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	
	protected static void checkCapacity(long capacity) {
		if (capacity > Integer.MAX_VALUE || capacity < 0) {
			new ArgumentError("Illegal capacity: %d", capacity)
				.throwSelfNative();
		}
	}
	
	private CandyObject[] elements;
	private int length;
	
	private boolean isInProcessOfStr;
	
	protected ArrayObj() {
		super(ARRAY_CLASS);
	}
	
	public ArrayObj(long n) {
		super(ARRAY_CLASS);
		initArray(n);
	}
	
	public ArrayObj(CandyObject[] elements) {
		super(ARRAY_CLASS);
		this.elements = elements;
		this.length = elements.length;
	}
	
	public ArrayObj(CandyObject[] elements, int size) {
		super(ARRAY_CLASS);
		this.elements = elements;
		this.length = size;
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
	
	public void append(CandyObject obj) {
		ensureCapacity(length + 1);
		elements[length ++] = obj;
	}
	
	public void addAll(CandyObject[] arr, int len) {
		addAll(this.length, arr, len);
	}
	
	public void addAll(int index, CandyObject[] arr, int len) {
		ensureCapacity(this.length + len);
		System.arraycopy(
			elements, index, elements, index + len, this.length-index);
		System.arraycopy(arr, 0, elements, index, len);
		this.length += len;
	}
	
	public CandyObject[] getBuiltinArray() {
		return elements;
	}
	
	public CandyObject get(int index) {
		index = RangeError.checkIndex(index, length);
		return elements[index];
	}
	
	public int length() {
		return length;
	}
	
	private int indexOf(CNIEnv env, CandyObject obj) {
		for (int i = 0; i < length; i ++) {
			if (elements[i].callEquals(env, obj).value()) {
				return i;
			}
		}
		return -1;
	}
	
	private int lastIndexOf(CNIEnv env, CandyObject obj) {
		for (int i = length-1; i >= 0; i --) {
			if (elements[i].callEquals(env, obj).value()) {
				return i;
			}
		}
		return -1;
	}
	
	private void insert(int index, CandyObject e) {
		if (index == length) {
			append(e);
			return;
		}
		ensureCapacity(length + 1);
		System.arraycopy(elements, index, elements, index + 1, length-index);
		elements[index] = e;
		length ++;
	}
	
	private boolean delete(CNIEnv env, CandyObject obj) {
		int i = indexOf(env, obj);
		if (i == -1) {
			return false;
		}
		deleteAt(i);
		return true;
	}
	
	public CandyObject deleteAt(int index) {
		CandyObject oldValue = elements[index];
		length--;
		if (index != length) {
			System.arraycopy(elements, index+1, elements, index, length-index);
		}
		elements[length] = null;
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
		int n = length - to;
		System.arraycopy(elements, to, elements, from, n);
		for (int i = from + n; i < length; i ++) {
			elements[i] = null;
		}
		length -= to-from;
	}
	
	public TupleObj toTuple() {
		return new TupleObj(Arrays.copyOf(elements, length));
	}
	
	@Override
	public CandyObject getItem(CNIEnv env, CandyObject key) {
		return ObjectHelper.preventNull(
			elements[IndexHelper.asIndex(key, length)]
		);
	}

	@Override
	public CandyObject setItem(CNIEnv env, CandyObject key, CandyObject value) {
		elements[IndexHelper.asIndex(key, length)] = value;
		return value;
	}

	@Override
	public CandyObject add(CNIEnv env, CandyObject operand) {
		TypeError.checkTypeMatched(ARRAY_CLASS, operand);
		return new ArrayObj(
			ArrayUtils.mergeArray(elements, ((ArrayObj) operand).elements)
		);
	}

	@Override
	protected CandyObject mul(CNIEnv env, CandyObject operand) {
		long repat = ObjectHelper.asInteger(operand);
		ArgumentError.checkValueTooLarge(repat, "repeat");
		return new ArrayObj(ArrayUtils.repeat(elements, length, (int) repat));
	}
	
	@Override
	public CandyObject iterator(CNIEnv env) {
		return new IteratorObj.ArrIterator(elements, length);
	}

	@Override
	public IntegerObj hashCode(CNIEnv env) {
		int hash = 0;
		for (int i = 0; i < length; i ++) {
			hash = hash * 31 + 
				(int) elements[i].callHashCode(env).intValue();
		}
		return IntegerObj.valueOf(hash);
	}

	@Override
	public BoolObj equals(CNIEnv env, CandyObject operand) {
		if (this == operand) {
			return BoolObj.TRUE;
		}
		if (!(operand instanceof ArrayObj)) {
			return super.equals(env, operand);
		}
		ArrayObj arr = (ArrayObj) operand;
		if (arr.length != length) {
			return BoolObj.FALSE;
		}
		final int SIZE = this.length;
		for (int i = 0; i < SIZE; i ++) {	
			BoolObj result = get(i).callEquals(env, arr.get(i));
			if (!result.value()) {
				return BoolObj.FALSE;
			}
		}
		return BoolObj.TRUE;
	}

	@Override
	public StringObj str(CNIEnv env) {
		if (length == 0) {
			return StringObj.EMPTY_LIST;
		}
		if (isInProcessOfStr) {
			return StringObj.RECURSIVE_LIST;
		}
		isInProcessOfStr = true;
		StringBuilder builder = new StringBuilder("[");
		builder.append(ArrayHelper.toString(env, elements, 0, length, ", "));
		builder.append("]");
		isInProcessOfStr = false;
		return StringObj.valueOf(builder.toString());
	}
	
	@NativeMethod(name = Names.METHOD_INITALIZER, argc = 2)
	public CandyObject initalizer(CNIEnv env, CandyObject[] args) {
		long initalCapacity = ObjectHelper.asInteger(args[0]);
		CandyObject defaultElement = args[1];
		initArray(initalCapacity);
		
		if (defaultElement.isCallable()) {
			CallableObj callable = (CallableObj) defaultElement;
			for (int i = 0; i < initalCapacity; i ++) {
				CandyObject element = 
					callable.callExeUser(env, IntegerObj.valueOf(i));
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
	public CandyObject append(CNIEnv env, CandyObject[] args) {
		append(args[0]);
		return this;
	}
	
	@NativeMethod(name = "appendAll", argc = 1, varArgsIndex = 0)
	public CandyObject appendAll(CNIEnv env, CandyObject[] args) {
		if (args[0] == NullPointer.nil()) {
			return this;
		}
		ArrayObj arr = (ArrayObj) args[0];
		addAll(arr.elements, arr.length);
		return this;
	}
	
	@NativeMethod(name = "insert", argc = 2)
	public CandyObject insert(CNIEnv env, CandyObject[] args) {
		int index = IndexHelper.asIndexForAdd(args[0], length);
		insert(index, args[1]);
		return args[1];
	}
	
	@NativeMethod(name = "insertAll", argc = 2, varArgsIndex = 1)
	public CandyObject insertAllAt(CNIEnv env, CandyObject[] args) {
		int index = IndexHelper.asIndexForAdd(args[0], length);
		if (args[1] == NullPointer.nil()) {
			return this;
		}
		ArrayObj arr = (ArrayObj) args[1];
		addAll(index, arr.elements, arr.length);
		return this;
	}
	
	@NativeMethod(name = "deleteAt", argc = 1)
	public CandyObject deleteAt(CNIEnv env, CandyObject[] args) {
		return deleteAt(IndexHelper.asIndex(args[0], length));
	}
	
	@NativeMethod(name = "delete", argc = 1)
	public CandyObject delete(CNIEnv env, CandyObject[] args) {
		return BoolObj.valueOf(delete(env, args[0]));
	}
	
	@NativeMethod(name = "deleteRange", argc = 2)
	public CandyObject deleteRange(CNIEnv env, CandyObject[] args) {
		deleteRange(
			IndexHelper.asIndex(args[0], length),
			IndexHelper.asIndexForAdd(args[1], length)
		);
		return this;
	}
	
	@NativeMethod(name = "contains", argc = 1)
	public CandyObject contains(CNIEnv env, CandyObject[] args) {
		return BoolObj.valueOf(indexOf(env, args[0]) != -1);
	}
	
	@NativeMethod(name = "indexOf", argc = 1)
	public CandyObject indexOf(CNIEnv env, CandyObject[] args) {
		return IntegerObj.valueOf(indexOf(env, args[0]));
	}
	
	@NativeMethod(name = "lastIndexOf", argc = 1)
	public CandyObject lastIndexOf(CNIEnv env, CandyObject[] args) {
		return IntegerObj.valueOf(lastIndexOf(env, args[0]));
	}
	
	@NativeMethod(name = "swap", argc = 2)
	public CandyObject swap(CNIEnv env, CandyObject[] args) {
		int i = IndexHelper.asIndex(args[0], length);
		int j = IndexHelper.asIndex(args[1], length);
		
		CandyObject tmp = elements[i];
		elements[i] = elements[j];
		elements[j] = tmp;
		return this;
	}
	
	@NativeMethod(name = "copy")
	public CandyObject copy(CNIEnv env, CandyObject[] args) {
		return new ArrayObj(Arrays.copyOf(elements, length));
	}
	
	@NativeMethod(name = "copyRange", argc = 2)
	public CandyObject copyRange(CNIEnv env, CandyObject[] args) {
		int from = IndexHelper.asIndex(args[0], length);
		int to = IndexHelper.asIndexForAdd(args[1], length);
		return new ArrayObj(Arrays.copyOfRange(elements, from, to));
	}
	
	@NativeMethod(name = "sort") 
	public CandyObject sort(CNIEnv env, CandyObject[] args) {
		Arrays.sort(elements, 0, length, ObjectHelper.newComparator(env));
		return this;
	}
	
	@NativeMethod(name = "reverse")
	public CandyObject reverse(CNIEnv env, CandyObject[] args) {
		int half = length/2;
		for (int i = 0; i < half; i ++) {
			CandyObject tmp = elements[i];
			elements[i] = elements[length-1-i];
			elements[length-1-i] = tmp;
		}
		return this;
	}
	
	private static Random rad;
	@NativeMethod(name = "shuffle")
	public CandyObject shuffle(CNIEnv env, CandyObject[] args) {
		if (rad == null) {
			rad = new Random();
		}
		for (int i = length-1; i > 1; i --) {
			int j = rad.nextInt(i+1);
			CandyObject tmp = elements[i];
			elements[i] = elements[j];
			elements[j] = tmp;
		}
		return this;
	}
	
	@NativeMethod(name = "map", argc=1)
	public CandyObject map(CNIEnv env, CandyObject[] args) {
		CallableObj mapper = TypeError.requiresCallable(args[0]);
		final int SIZE = length;
		for (int i = 0; i < SIZE; i ++) {
			elements[i] = 
				mapper.callExeUser(env, IntegerObj.valueOf(i), elements[i]);
		}
		return this;
	}
	
	@NativeMethod(name = "foreach", argc = 1)
	public CandyObject foreach(CNIEnv env, CandyObject[] args) {
		CallableObj walker = TypeError.requiresCallable(args[0]);
		final int SIZE = length;
		for (int i = 0; i < SIZE; i ++) {
			walker.callExeUser(env, IntegerObj.valueOf(i), elements[i]);
		}
		return this;
	}
	
	@NativeMethod(name = "toTuple")
	public CandyObject toTuple(CNIEnv env, CandyObject[] args) {
		return toTuple();
	}
	
	@NativeMethod(name = "length")
	public CandyObject size(CNIEnv env, CandyObject[] args) {
		return IntegerObj.valueOf(length);
	}
	
	@NativeMethod(name = "clear")
	public CandyObject clear(CNIEnv env, CandyObject[] args) {
		for (int i = 0; i < length; i ++) {
			elements[i] = null;
		}
		length = 0;
		return this;
	}
	
	@NativeMethod(name = "isEmpty")
	public CandyObject isEmpty(CNIEnv env, CandyObject[] args) {
		return BoolObj.valueOf(length == 0);
	}
}
