package com.nano.candy.interpreter.i2.builtin.type;
import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.IteratorObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.ObjectClass;
import com.nano.candy.interpreter.i2.cni.FasterNativeMethod;
import com.nano.candy.interpreter.i2.rtda.Variable;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import java.util.Iterator;
import java.util.Map;

public abstract class IteratorObj extends BuiltinObject {
	
	public static class RangeIterator extends IteratorObj {
		private int v;
		private long left, right;

		public RangeIterator(long left, long right) {
			this.left = left;
			this.right = right;
			this.v = left > right ? -1 : 1;
		}

		@Override
		public final boolean hasNext(VM vm) {
			return left != right;
		}

		@Override
		public final CandyObject next(VM vm) {
			IntegerObj next = IntegerObj.valueOf(left);
			left += v;
			return next;
		}
	}
	
	public static class MapIterator extends IteratorObj {
		private Iterator<Map.Entry<String, CandyObject>> iterator;
		
		public MapIterator(Iterator<Map.Entry<String, CandyObject>> iterator) {
			this.iterator = iterator;
		}
		@Override
		public final boolean hasNext(VM vm) {
			return iterator.hasNext();
		}

		@Override
		public final CandyObject next(VM vm) {
			Map.Entry<String, CandyObject> entry = iterator.next();
			CandyObject[] kv = {
				StringObj.valueOf(entry.getKey()), entry.getValue()
			};
			return new TupleObj(kv);
		}
	}
	
	public static class VarableIterator extends IteratorObj {
		private Iterator<Variable> iterator;

		public VarableIterator(Iterator<Variable> iterator) {
			this.iterator = iterator;
		}
		
		@Override
		public final boolean hasNext(VM vm) {
			return iterator.hasNext();
		}

		@Override
		public final CandyObject next(VM vm) {
			Variable variable = iterator.next();
			CandyObject[] kv = {
				StringObj.valueOf(variable.getName()), variable.getValue()
			};
			return new TupleObj(kv);
		}
	}
	
	public static class ArrIterator extends IteratorObj {
		private int i;
		private int size;
		private CandyObject[] elements;

		public ArrIterator(CandyObject[] elements, int size) {
			this.elements = elements;
			this.size = size;
		}

		@Override
		public final boolean hasNext(VM vm) {
			return i < size;
		}

		@Override
		public final CandyObject next(VM vm) {
			return elements[i ++];
		}
	}
	
	private FasterNativeMethod next;
	private FasterNativeMethod hasNext;
	
	public IteratorObj() {
		super(ObjectClass.getObjClass());
		next = new FasterNativeMethod(
			"Iterator", Names.METHOD_ITERATOR_NEXT, 0, 
			this::next
		);
		
		hasNext = new FasterNativeMethod(
			"Iterator", Names.METHOD_ITERATOR_HAS_NEXT, 0, 
			this::hasNext
		);
	}

	@Override
	public CandyObject getAttr(VM vm, String attr) {
		switch (attr) {
			case Names.METHOD_ITERATOR_NEXT:
				return next;
			case Names.METHOD_ITERATOR_HAS_NEXT:
				return hasNext;
		}
		return super.getAttr(vm, attr);
	}
	
	public final CandyObject hasNext(VM vm, int argc) {
		return BoolObj.valueOf(hasNext(vm));
	}
	
	public final CandyObject next(VM vm, int argc) {
		return next(vm);
	}
	
	public abstract boolean hasNext(VM vm);
	public abstract CandyObject next(VM vm);
}
