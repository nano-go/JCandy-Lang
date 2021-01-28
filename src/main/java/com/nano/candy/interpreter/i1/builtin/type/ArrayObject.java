package com.nano.candy.interpreter.i1.builtin.type;
import com.nano.candy.interpreter.error.ArgumentError;
import com.nano.candy.interpreter.error.CandyRuntimeError;
import com.nano.candy.interpreter.error.TypeError;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyHelper;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.classes.BuiltinClassImpl;
import com.nano.candy.interpreter.i1.builtin.classes.NumberClass;
import com.nano.candy.interpreter.i1.builtin.classes.annotation.BuiltinClass;
import com.nano.candy.interpreter.i1.builtin.func.annotation.BuiltinMethod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

@BuiltinClass("Array")
public class ArrayObject extends CandyObject {
	
	public static final BuiltinClassImpl ARRAY_CLASS = BuiltinClassImpl.newBuiltinClass(ArrayObject.class);
	
	private static class ArrayIterator extends Iterator.IteratorObject {
		private ArrayList<CandyObject> list;
		private int curIndex;
		public ArrayIterator(ArrayList<CandyObject> list) {
			this.list = list;
			this.curIndex = 0;
		}

		@Override
		public boolean hasNext() {
			return curIndex != list.size();
		}

		@Override
		public CandyObject next() {
			return list.get(curIndex ++);
		}	
	}
	
	public ArrayList<CandyObject> elements; 
	
	public ArrayObject() {
		this(new ArrayList<>());
	}
	
	public ArrayObject(ArrayList<CandyObject> elements) {
		super(ARRAY_CLASS);
		this.elements = Objects.requireNonNull(elements);
	}

	public void ensureCapacity(int minCapacity) {
		elements.ensureCapacity(minCapacity);
	}

	public void append(CandyObject element) {
		elements.add(element);
	}
	
	public CandyObject get(int index) {
		return elements.get(index);
	}
	
	public CandyObject set(int index, CandyObject element) {
		return elements.set(index, element);
	}
	
	public CandyObject removeAt(int index) {
		return elements.remove(index);
	}
	
	public boolean remove(CandyObject element) {
		return elements.remove(element);
	}
	
	public int size() {
		return elements.size();
	}
	
	@Override
	public CandyObject setItem(CandyObject key, CandyObject value) {
		if (key instanceof IntegerObject) {
			return set((int)((IntegerObject) key).intValue(), value);
		}
		return super.setItem(key, value);
	}

	@Override
	public CandyObject getItem(CandyObject key) {
		if (key instanceof IntegerObject) {
			return get((int)((IntegerObject) key).intValue());
		}
		return super.delItem(key);
	}
	
	@Override
	public CandyObject delItem(CandyObject key) {
		if (key instanceof IntegerObject) {
			return removeAt((int)((IntegerObject) key).intValue());
		}
		return super.getItem(key);
	}
	
	@Override
	public CandyObject iterator() {
		return new ArrayIterator(elements);
	}

	@Override
	public BooleanObject equalTo(CandyObject obj) {
		if (obj == this) return BooleanObject.TRUE;
		if (obj instanceof ArrayObject) {
			return BooleanObject.valueOf(
				elements.equals(((ArrayObject)obj).elements)
			);
		}
		return super.equalTo(obj);
	}

	@Override
	public StringObject stringValue(AstInterpreter interpreter) {
		java.util.Iterator<CandyObject> i = elements.iterator();
		if (!i.hasNext()) {
			return StringObject.of("[]");
		}
		StringBuilder builder = new StringBuilder("[");
		while (true) {
			builder.append(i.next().stringValue(interpreter).value());
			if (!i.hasNext()) {
				break;
			}
			builder.append(", ");
		}
		builder.append("]");
		return StringObject.of(builder.toString());
	}
	
	/*===================== Built-in Methods ===================*/

	/**
	 * Built-in Method: Initializer(length, defaultElementGen)
	 *
	 * <p>length: the initial length of an array.</p>
	 * <p>defaultElementGen: If the argument is a Callable object, 
	 *                       it's generator, otherwise default element.</p>
	 */
	@BuiltinMethod(value = "", argc = 2)
	public CandyObject initializer(AstInterpreter interpreter, CandyObject[] args) {
		TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, args[0]);
		int length = (int)((IntegerObject)args[0]).intValue();
		if (length < 0) {
			throw new CandyRuntimeError("Illegal Initial Length: %d.", length);
		}
		ensureCapacity(length);
		if (args[1] instanceof Callable) {
			Callable generator = (Callable) args[1];
			ArgumentError.checkArguments(1, generator.arity());
			CandyObject[] callableArgs = new CandyObject[1];
			for (int i = 0; i < length; i++) {
				callableArgs[0] = new IntegerObject(i);
				CandyObject element = generator.onCall(
					interpreter, callableArgs
				);
				append(element);
			}
		} else {
			CandyObject defaultElement = args[1];
			for (int i = 0; i < length; i ++) {
				append(defaultElement);
			}
		}
		return this;
	}

	@BuiltinMethod(value = "append", argc = 1) 
	public CandyObject append(AstInterpreter interpreter, CandyObject[] args) {
		append(args[0]);
		return this;
	}
	
	@BuiltinMethod(value = "insert", argc = 2) 
	public CandyObject insert(AstInterpreter interpreter, CandyObject[] args) {
		TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, args[0]);
		int index = (int)((IntegerObject)args[0]).intValue();
		elements.add(index, args[1]);
		return this;
	}

	@BuiltinMethod(value = "removeAt", argc = 1)
	public CandyObject removeAt(AstInterpreter interpreter, CandyObject[] args) {
		TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, args[0]);
		return removeAt((int)((IntegerObject)args[0]).intValue());
	}

	@BuiltinMethod(value = "remove", argc = 1)
	public CandyObject remove(AstInterpreter interpreter, CandyObject[] args) {
		return BooleanObject.valueOf(remove(args[0]));
	}

	@BuiltinMethod(value = "set", argc = 2)
	public CandyObject set(AstInterpreter interpreter, CandyObject[] args) {
		TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, args[0]);
		return set((int)((IntegerObject)args[0]).intValue(), args[1]);
	}

	@BuiltinMethod(value = "get", argc = 1)
	public CandyObject get(AstInterpreter interpreter, CandyObject[] args) {
		TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, args[0]);
		return get((int)((IntegerObject)args[0]).intValue());
	}
	
	@BuiltinMethod(value="swap", argc = 2)
	public CandyObject swap(AstInterpreter interpreter, CandyObject[] args) {
		TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, args[0]);
		TypeError.checkTypeMatched(NumberClass.INTEGER_CLASS, args[1]);
		int p = (int)((IntegerObject)args[0]).intValue();
		int q = (int)((IntegerObject)args[1]).intValue();
		Collections.swap(elements, p, q);
		return this;
	}
	
	@BuiltinMethod("randElement")
	public CandyObject randElement(AstInterpreter interpreter, CandyObject[] args) {
		if (elements.isEmpty()) {
			throw new CandyRuntimeError("Empty array.");
		}
		return elements.get((int)(Math.random() * elements.size()));
	}
	
	@BuiltinMethod("sort")
	public CandyObject sort(AstInterpreter interpreter, CandyObject[] args) {
		Collections.sort(elements, CandyHelper.newComparator());
		return this;
	}
	
	@BuiltinMethod("reverse")
	public CandyObject reverse(AstInterpreter interpreter, CandyObject[] args) {
		Collections.reverse(elements);
		return this;
	}

	@BuiltinMethod("size")
	public CandyObject size(AstInterpreter interpreter, CandyObject[] args) {
		return new IntegerObject(size());
	}
	
}

