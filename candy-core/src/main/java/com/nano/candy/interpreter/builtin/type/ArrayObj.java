package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.builtin.type.error.NativeError;
import com.nano.candy.interpreter.builtin.type.error.RangeError;
import com.nano.candy.interpreter.builtin.type.error.StateError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.builtin.utils.ArrayHelper;
import com.nano.candy.interpreter.builtin.utils.IndexHelper;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.builtin.utils.OptionalArg;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeMethod;
import com.nano.candy.std.Names;
import com.nano.candy.utils.ArrayUtils;
import java.util.Arrays;
import java.util.Random;

@NativeClass(name = "Array", isInheritable=true)
public final class ArrayObj extends CandyObject {
	
	public static final CandyClass ARRAY_CLASS = 
		NativeClassRegister.generateNativeClass(ArrayObj.class);
	
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;	
	public static final CandyObject[] EMPTY_ARRAY = new CandyObject[0];
	
	public static final ArrayObj emptyArray() {
		return new ArrayObj(EMPTY_ARRAY); 
	}
	
	protected static void checkCapacity(long capacity) {
		if (capacity > Integer.MAX_VALUE || capacity < 0) {
			new ArgumentError("Illegal capacity: %d", capacity)
				.throwSelfNative();
		}
	}
	
	protected CandyObject[] elements;
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
			elements = Arrays.copyOf(elements, newArrayCapacity(minCapacity));
		}
	}
	
	private int newArrayCapacity(int minCapacity) {
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
		return newCapacity;
	}
	
	public void append(CandyObject obj) {
		ensureCapacity(length + 1);
		elements[length ++] = obj;
	}
	
	public void insertAll(CandyObject[] arr, int len) {
		insertAll(this.length, arr, len);
	}
	
	public void insertAll(int index, CandyObject[] arr, int len) {
		ensureCapacity(this.length + len);
		System.arraycopy(
			elements, index, elements, index + len, this.length-index);
		System.arraycopy(arr, 0, elements, index, len);
		this.length += len;
	}
	
	public CandyObject get(int index) {
		index = RangeError.checkIndex(index, length);
		return elements[index];
	}
	
	public void replaceRange(Range range, CandyObject[] newArray) {
		replaceRange(range, newArray, newArray.length);
	}
	
	public void replaceRange(Range range, CandyObject[] newArray, int newArrayLen) {
		int begin = IndexHelper.asIndex(range.getLeftObj(), length);
		int end = IndexHelper.asIndexForAdd(range.getRightObj(), length);
		privateReplaceRange(begin, end, newArray, newArrayLen);
	}
	
	public void replaceRange(int begin, int end, CandyObject[] newArray) {
		replaceRange(begin, end, newArray);					 
	}
	
	public void replaceRange(int begin, int end, CandyObject[] newArray, 
	                         int newArrayLen) {
		begin = RangeError.checkIndex(begin, length);
		end = RangeError.checkIndexForAdd(end, length);
		privateReplaceRange(begin, end, newArray, newArrayLen);
	}
	
	/**
	 * We assume that the begin index and the end index are invalid.
	 */
	private void privateReplaceRange(int begin, int end, 
	                                 CandyObject[] newArray, 
	                                 int newArrayLen) {
		if (begin >= end) {
			insertAll(begin, newArray, newArrayLen);
			return;
		}
		int len = this.length-(end-begin)+newArrayLen;
		if (len > elements.length) {
			CandyObject[] narr = new CandyObject[newArrayCapacity(len)];
			System.arraycopy(elements, 0, narr, 0, begin);
			System.arraycopy(newArray, 0, narr, begin, newArrayLen);
			System.arraycopy(elements, end, narr, begin + newArrayLen, this.length - end);
			this.elements = narr;
		} else {
			System.arraycopy(
				elements, end, 
				elements, begin + newArrayLen, this.length-end);
			System.arraycopy(newArray, 0, elements, begin, newArrayLen);
		}
		this.length = len;
	}
	
	public CandyObject[] subarray(Range range) {
		int begin = IndexHelper.asIndex(range.getLeftObj(), length);
		int end = IndexHelper.asIndexForAdd(range.getRightObj(), length);
		return privateSubarray(begin, end);
	}
	
	public CandyObject[] subarray(int begin, int end) {
		begin = RangeError.checkIndex(begin, length);
		end = RangeError.checkIndexForAdd(begin, length);
		return privateSubarray(begin, end);
	}
	
	/**
	 * We assume that the begin index and the end index are invalid.
	 */
	private CandyObject[] privateSubarray(int begin, int end) {
		if (end - begin <= 0) {
			return EMPTY_ARRAY;
		}
		return Arrays.copyOfRange(elements, begin, end);
	}
	
	public int length() {
		return length;
	}
	
	public int indexOf(CNIEnv env, CandyObject obj) {
		for (int i = 0; i < length; i ++) {
			if (elements[i].callEquals(env, obj).value()) {
				return i;
			}
		}
		return -1;
	}
	
	public int lastIndexOf(CNIEnv env, CandyObject obj) {
		for (int i = length-1; i >= 0; i --) {
			if (elements[i].callEquals(env, obj).value()) {
				return i;
			}
		}
		return -1;
	}
	
	public void insert(int index, CandyObject e) {
		if (index == length) {
			append(e);
			return;
		}
		ensureCapacity(length + 1);
		System.arraycopy(elements, index, elements, index + 1, length-index);
		elements[index] = e;
		length ++;
	}
	
	public boolean delete(CNIEnv env, CandyObject obj) {
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
		if (key instanceof Range) {
			return new ArrayObj(subarray((Range) key));
		}
		return ObjectHelper.preventNull(
			elements[IndexHelper.asIndex(key, length)]
		);
	}

	@Override
	public CandyObject setItem(CNIEnv env, CandyObject key, CandyObject value) {
		if (key instanceof Range) {
			if (value instanceof ArrayObj) {
				ArrayObj arr = (ArrayObj) value;
				replaceRange((Range) key, arr.elements, arr.length);
			} else {
				replaceRange((Range) key, new CandyObject[]{value});
			}
			return value;
		}
		elements[IndexHelper.asIndex(key, length)] = value;
		return value;
	}

	@Override
	public CandyObject add(CNIEnv env, CandyObject operand) {
		TypeError.checkTypeMatched(ARRAY_CLASS, operand);
		ArrayObj arr = (ArrayObj) operand;
		CandyObject[] newElements = new CandyObject[this.length + arr.length];
		System.arraycopy(this.elements, 0, newElements, 0, this.length);
		System.arraycopy(arr.elements, 0, newElements, this.length, arr.length);
		return new ArrayObj(newElements);
	}

	@Override
	protected CandyObject mul(CNIEnv env, CandyObject operand) {
		long repat = ObjectHelper.asInteger(operand);
		ArgumentError.checkValueTooLarge(repat, "repeat");
		return new ArrayObj(ArrayUtils.repeat(elements, length, (int) repat));
	}

	@Override
	protected CandyObject lshift(CNIEnv env, CandyObject operand) {
		append(operand);
		return this;
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
	
	@NativeMethod(name = Names.METHOD_INITALIZER)
	protected CandyObject initalizer(CNIEnv env, long initialCapacity, CandyObject defElement) {
		initArray(initialCapacity);
		
		if (defElement.isCallable()) {
			CallableObj callable = (CallableObj) defElement;
			for (int i = 0; i < initialCapacity; i ++) {
				CandyObject element = 
					callable.call(env, IntegerObj.valueOf(i));
				append(element);
			}
		} else {
			for (int i = 0; i < initialCapacity; i ++) {
				append(defElement);
			}
		}	
		return this;
	}
	
	@NativeMethod(name = "append")
	protected CandyObject append(CNIEnv env, CandyObject element) {
		append(element);
		return this;
	}
	
	@NativeMethod(name = "appendAll",  varArgsIndex = 0)
	protected CandyObject appendAll(CNIEnv env, ArrayObj elements) {
		insertAll(elements.elements, elements.length);
		return this;
	}
	
	@NativeMethod(name = "insert")
	protected CandyObject insert(CNIEnv env, CandyObject indexObj, CandyObject element) {
		int index = IndexHelper.asIndexForAdd(indexObj, length);
		insert(index, element);
		return element;
	}
	
	@NativeMethod(name = "insertAll", varArgsIndex = 1)
	protected CandyObject insertAllAt(CNIEnv env, CandyObject indexObj, ArrayObj elements) {
		int index = IndexHelper.asIndexForAdd(indexObj, length);
		insertAll(index, elements.elements, elements.length);
		return this;
	}
	
	@NativeMethod(name = "deleteAt")
	protected CandyObject deleteAt(CNIEnv env, CandyObject indexObj) {
		return deleteAt(IndexHelper.asIndex(indexObj, length));
	}
	
	@NativeMethod(name = "delete")
	protected CandyObject deleteMet(CNIEnv env, CandyObject element) {
		return BoolObj.valueOf(delete(env, element));
	}
	
	@NativeMethod(name = "deleteRange")
	protected CandyObject deleteRange(CNIEnv env, CandyObject from, CandyObject to) {
		deleteRange(
			IndexHelper.asIndex(from, length),
			IndexHelper.asIndexForAdd(to, length)
		);
		return this;
	}
	
	@NativeMethod(name = "contains")
	protected CandyObject contains(CNIEnv env, CandyObject element) {
		return BoolObj.valueOf(indexOf(env, element) != -1);
	}
	
	@NativeMethod(name = "indexOf")
	protected CandyObject indexOfMet(CNIEnv env, CandyObject element) {
		return IntegerObj.valueOf(indexOf(env, element));
	}
	
	@NativeMethod(name = "lastIndexOf")
	protected CandyObject lastIndexOfMet(CNIEnv env, CandyObject element) {
		return IntegerObj.valueOf(lastIndexOf(env, element));
	}
	
	@NativeMethod(name = "swap")
	protected CandyObject swap(CNIEnv env, CandyObject io, CandyObject jo) {
		int i = IndexHelper.asIndex(io, length);
		int j = IndexHelper.asIndex(jo, length);
		
		CandyObject tmp = elements[i];
		elements[i] = elements[j];
		elements[j] = tmp;
		return this;
	}
	
	@NativeMethod(name = "copy")
	protected CandyObject copy(CNIEnv env) {
		return new ArrayObj(Arrays.copyOf(elements, length));
	}
	
	@NativeMethod(name = "sort") 
	protected CandyObject sort(CNIEnv env) {
		Arrays.sort(elements, 0, length, ObjectHelper.newComparator(env));
		return this;
	}
	
	@NativeMethod(name = "sortBy") 
	protected CandyObject sortBy(CNIEnv env, CandyObject comparator) {
		Arrays.sort(elements, 0, length, ObjectHelper.newComparator(env, comparator));
		return this;
	}
	
	@NativeMethod(name = "reverse")
	protected CandyObject reverse(CNIEnv env) {
		int half = length/2;
		for (int i = 0; i < half; i ++) {
			CandyObject tmp = elements[i];
			elements[i] = elements[length-1-i];
			elements[length-1-i] = tmp;
		}
		return this;
	}
	
	@NativeMethod(name = "pop")
	protected CandyObject pop(CNIEnv env) {
		if (length == 0) {
			new StateError("This is an empty stack.").throwSelfNative();
		}
		CandyObject r = elements[--length];
		elements[length] = null;
		return r;
	}
	
	@NativeMethod(name = "push")
	protected CandyObject push(CNIEnv env, CandyObject element) {
		append(element);
		return this;
	}
	
	@NativeMethod(name = "peek")
	protected CandyObject peek(CNIEnv env, OptionalArg k) {
		long kVal = ObjectHelper.asInteger(k.getValue(0));
		ArgumentError.checkValueTooLarge(kVal, "peek(k)");
		if (kVal >= length) {
			new ArgumentError(
				"The length of the array is %d, but the arg 'peek(k)' is %d.",
				length, kVal)
				.throwSelfNative();
		}
		return get(length-(int)kVal-1);
	}
	
	private static Random rad;
	@NativeMethod(name = "shuffle")
	protected CandyObject shuffle(CNIEnv env) {
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
	
	@NativeMethod(name = "map")
	protected CandyObject map(CNIEnv env, CallableObj mapper) {
		CandyObject[] newElements = new CandyObject[length];
		for (int i = 0; i < length; i ++) {
			newElements[i] = 
				mapper.flexiblyCall(env, 1, elements[i], IntegerObj.valueOf(i));
		}
		return new ArrayObj(newElements);
	}
	
	@NativeMethod(name = "filter")
	protected CandyObject filter(CNIEnv env, CallableObj filter) {
		CandyObject[] newElements = new CandyObject[length];
		int size = 0;
		for (int i = 0; i < length; i ++) {
			boolean accept = filter
				.flexiblyCall(env, 1, elements[i], IntegerObj.valueOf(i))
				.boolValue(env).value();
			if (accept) {
				newElements[size ++] = elements[i];
			}
		}
		return new ArrayObj(newElements, size);
	}
	
	@NativeMethod(name = "reduce")
	protected CandyObject reduce(CNIEnv env, 
	                             OptionalArg initialValArg, CallableObj operator) {
		CandyObject total = initialValArg.getValue(NullPointer.nil());
		if (length == 0) {
			return total;
		}
		int i = 0;
		if (total == NullPointer.nil()) {
			total = elements[0];
			i = 1;
		}
		for (;i < length; i ++) {
			total = operator.flexiblyCall(
				env, 1, total, elements[i], IntegerObj.valueOf(i));
		}
		return total;
	}
	
	@NativeMethod(name = "reduceRight")
	protected CandyObject reduceRight(CNIEnv env, 
	                                  OptionalArg initialValArg, CallableObj operator) {
		CandyObject total = initialValArg.getValue(NullPointer.nil());
		if (length == 0) {
			return total;
		}
		int i = length-1;
		if (total == NullPointer.nil()) {
			total = elements[i];
			i --;
		}
		for (;i >= 0; i --) {
			total = operator.flexiblyCall(
				env, 1, total, elements[i], IntegerObj.valueOf(i));
		}
		return total;
	}
	
	@NativeMethod(name = "foreach")
	protected CandyObject foreach(CNIEnv env, CallableObj walker) {
		final int SIZE = length;
		for (int i = 0; i < SIZE; i ++) {
			walker.flexiblyCall(env, 1, elements[i], IntegerObj.valueOf(i));
		}
		return this;
	}
	
	@NativeMethod(name = "max")
	protected CandyObject max(CNIEnv env, OptionalArg comparator) {
		if (length == 0) {
			return null;
		}
		CandyObject comparatorObj = comparator.getValue(NullPointer.nil());
		CandyObject max = elements[0];
		if (comparatorObj == NullPointer.nil()){
			for (int i = 1; i < length; i ++) {
				if (elements[i].callGt(env, max).boolValue(env).value()) {
					max = elements[i];
				}
			}
		} else {
			CallableObj comparatorFn = TypeError.requiresCallable(comparatorObj);
			for (int i = 1; i < length; i ++) {
				long result = ObjectHelper.asInteger(
					comparatorFn.call(env, max, elements[i]));
				if (result < 0) {
					max = elements[i];
				}
			}
		}
		return max;
	}
	
	@NativeMethod(name = "min")
	protected CandyObject min(CNIEnv env, OptionalArg comparator) {
		if (length == 0) {
			return null;
		}
		CandyObject comparatorObj = comparator.getValue(NullPointer.nil());
		CandyObject min = elements[0];
		if (comparatorObj == NullPointer.nil()){
			for (int i = 1; i < length; i ++) {
				if (elements[i].callLt(env, min).boolValue(env).value()) {
					min = elements[i];
				}
			}
		} else {
			CallableObj comparatorFn = TypeError.requiresCallable(comparatorObj);
			for (int i = 1; i < length; i ++) {
				long result = ObjectHelper.asInteger(
					comparatorFn.call(env, min, elements[i]));
				if (result > 0) {
					min = elements[i];
				}
			}
		}
		return min;
	}
	
	@NativeMethod(name = "length")
	protected CandyObject size(CNIEnv env) {
		return IntegerObj.valueOf(length);
	}
	
	@NativeMethod(name = "clear")
	protected CandyObject clear(CNIEnv env) {
		for (int i = 0; i < length; i ++) {
			elements[i] = null;
		}
		length = 0;
		return this;
	}
	
	@NativeMethod(name = "isEmpty")
	protected CandyObject isEmpty(CNIEnv env) {
		return BoolObj.valueOf(length == 0);
	}
}
