package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.error.IndexError;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.error.ArgumentError;
import com.nano.candy.interpreter.i2.error.NativeError;
import com.nano.candy.interpreter.i2.vm.VM;
import java.util.Arrays;

@BuiltinClass("Array")
public final class ArrayObj extends BuiltinObject {
	
	public static final CandyClass ARRAY_CLASS = BuiltinClassFactory.generate(ArrayObj.class);
	public static final CandyObject[] EMPTY_ARRAY = new CandyObject[0];
	
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	
	protected static void checkCapacity(long capacity) {
		if (capacity > Integer.MAX_VALUE || capacity < 0) {
			throw new ArgumentError("Illegal capacity: %d", capacity);
		}
	}
	
	private CandyObject[] elements;
	private int size;
	
	public ArrayObj() {
		super(ARRAY_CLASS);
	}
	
	public ArrayObj(long n) {
		super(ARRAY_CLASS);
		initArray(n);
	}
	
	protected ArrayObj(CandyObject[] elements) {
		super(ARRAY_CLASS);
		this.elements = elements;
		this.size = elements.length;
	}
	
	protected ArrayObj(CandyObject[] elements, int size) {
		super(ARRAY_CLASS);
		this.elements = elements;
		this.size = size;
		if (size > elements.length) {
			throw new ArgumentError("Illegal size: %d", size);
		}
	}
	
	private void initArray(long n) {
		if (n > Integer.MAX_VALUE || n < 0) {
			throw new ArgumentError("Illegal capacity: %d", n);
		}
		if (n == 0) {
			elements = EMPTY_ARRAY;
		} else {
			elements = new CandyObject[(int)n];
		}
	}
	
	private void ensureCapacity(int minCapacity) {
		if (minCapacity > elements.length) {
			if (elements == EMPTY_ARRAY) {
				elements = new CandyObject[10];
				return;
			}
			
			// see java.lang.ArrayList source code.
			int oldCapacity = elements.length;
			int newCapacity = oldCapacity + (oldCapacity >> 1);
			if (newCapacity - minCapacity < 0)
				newCapacity = minCapacity;
			if (newCapacity - MAX_ARRAY_SIZE > 0)
				newCapacity = hugeCapacity(minCapacity);
			elements = Arrays.copyOf(elements, newCapacity);
		}
	}

	/**
	 * see java.lang.ArrayList source code.
	 */
	private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0)
            throw new NativeError("Out of memory.");
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
	
	private int asIndex(CandyObject index) {
		long validIndex = ObjectHelper.asInteger(index);
		IndexError.checkIndex(validIndex, size);
		return (int) validIndex;
	}
	
	private int asIndexForInsert(CandyObject index) {
		long validIndex = ObjectHelper.asInteger(index);
		IndexError.checkIndex(validIndex, size + 1);
		return (int) validIndex;
	}
	
	public void append(CandyObject obj) {
		ensureCapacity(size + 1);
		elements[size ++] = obj;
	}
	
	public CandyObject get(int index) {
		IndexError.checkIndex(index, size);
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
	
	@Override
	public CandyObject getItem(CandyObject key) {
		return elements[asIndex(key)];
	}

	@Override
	public CandyObject setItem(CandyObject key, CandyObject value) {
		elements[asIndex(key)] = value;
		return value;
	}

	@Override
	public CandyObject add(VM vm, CandyObject operand) {
		append(operand);
		return this;
	}
	
	@Override
	public CandyObject iterator() {
		return new IteratorObj.ArrIterator(elements, size);
	}

	@Override
	public IntegerObj hashCode(VM vm) {
		int hashcode = 1;
		for (int i = 0; i < size; i ++) {
			hashcode = hashcode*31 +
				(int) get(i).hashCodeApiExeUser(vm).value;
		}
		return IntegerObj.valueOf(hashcode);
	}

	@Override
	public BoolObj equals(VM vm, CandyObject operand) {
		if (!(operand instanceof ArrayObj)) {
			return BoolObj.FALSE;
		}
		ArrayObj arr = (ArrayObj) operand;
		if (arr.size != size) {
			return BoolObj.FALSE;
		}
		final int SIZE = this.size;
		for (int i = 0; i < SIZE; i ++) {	
			BoolObj result = get(i).equalsApiExeUser(
				vm, arr.get(i));
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
		StringBuilder builder = new StringBuilder("[");
		int i = 0;
		for (;;) {
			StringObj obj = elements[i].strApiExeUser(vm);
			builder.append(obj);
			if (i >= size-1) {
				break;
			}
			builder.append(", ");
			i ++;
		}
		builder.append("]");
		return StringObj.valueOf(builder.toString());
	}
	
	@BuiltinMethod(name = "", argc = 2)
	public void initalizer(VM vm) {
		long initalCapacity = ObjectHelper.asInteger(vm.pop());
		CandyObject defaultElement = vm.pop();
		initArray(initalCapacity);
		
		if (defaultElement.isCallable()) {
			ArgumentError.checkArity(defaultElement, 1);
			CallableObj callable = (CallableObj) defaultElement;
			for (int i = 0; i < initalCapacity; i ++) {
				CandyObject element = ObjectHelper.callFunctionWithArgs(
					vm, callable, IntegerObj.valueOf(i));
				append(element);
			}
		} else {
			for (int i = 0; i < initalCapacity; i ++) {
				append(defaultElement);
			}
		}	
		vm.returnFromVM(this);
	}
	
	@BuiltinMethod(name = "append", argc = 1)
	public void append(VM vm) {
		append(vm.pop());
		vm.returnFromVM(this);
	}
	
	@BuiltinMethod(name = "deleteAt", argc = 1)
	public void deleteAt(VM vm) {
		vm.returnFromVM(deleteAt(asIndex(vm.pop())));
	}
	
	@BuiltinMethod(name = "delete", argc = 1)
	public void delete(VM vm) {
		vm.returnFromVM(BoolObj.valueOf(
			delete(vm, vm.pop()))
		);
	}
	
	@BuiltinMethod(name = "insert", argc = 2)
	public void insert(VM vm) {
		int index = asIndexForInsert(vm.pop());
		CandyObject e = vm.pop();
		insert(index, e);
		vm.returnFromVM(e);
	}
	
	@BuiltinMethod(name = "set", argc = 2)
	public void set(VM vm) {
		int index = asIndex(vm.pop());
		CandyObject value = vm.pop();
		elements[index] = value;
		vm.returnFromVM(value);
	}
	
	@BuiltinMethod(name = "get", argc = 1)
	public void get(VM vm) {
		int index = asIndex(vm.pop());
		vm.returnFromVM(elements[index]);
	}
	
	@BuiltinMethod(name = "contains", argc = 1)
	public void contains(VM vm) {
		boolean inArr = indexOf(vm, vm.pop()) != -1;
		vm.returnFromVM(BoolObj.valueOf(inArr));
	}
	
	@BuiltinMethod(name = "indexOf", argc = 1)
	public void indexOf(VM vm) {
		vm.returnFromVM(IntegerObj.valueOf(indexOf(vm, vm.pop())));
	}
	
	@BuiltinMethod(name = "lastIndexOf", argc = 1)
	public void lastIndexOf(VM vm) {
		vm.returnFromVM(IntegerObj.valueOf(lastIndexOf(vm, vm.pop())));
	}
	
	@BuiltinMethod(name = "size", argc = 0)
	public void size(VM vm) {
		vm.returnFromVM(IntegerObj.valueOf(size));
	}
	
	@BuiltinMethod(name = "swap", argc = 2)
	public void swap(VM vm) {
		int i = asIndex(vm.pop());
		int j = asIndex(vm.pop());
		
		CandyObject tmp = elements[i];
		elements[i] = elements[j];
		elements[j] = tmp;
		vm.returnFromVM(this);
	}
}
