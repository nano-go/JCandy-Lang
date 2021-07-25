package com.nano.candy.interpreter.i2.builtin.type;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.ObjectClass;
import com.nano.candy.interpreter.i2.builtin.type.IteratorObj;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import com.nano.candy.interpreter.i2.cni.FasterNativeMethod;
import com.nano.candy.interpreter.i2.runtime.Variable;
import com.nano.candy.std.Names;
import java.util.Iterator;
import java.util.Map;

public abstract class IteratorObj extends CandyObject {
	
	public static class RangeIterator extends IteratorObj {
		private int v;
		private long left, right;

		public RangeIterator(long left, long right) {
			this.left = left;
			this.right = right;
			this.v = left > right ? -1 : 1;
		}

		@Override
		public final boolean hasNext(CNIEnv env) {
			return left != right;
		}

		@Override
		public final CandyObject next(CNIEnv env) {
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
		public final boolean hasNext(CNIEnv env) {
			return iterator.hasNext();
		}

		@Override
		public final CandyObject next(CNIEnv env) {
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
		public final boolean hasNext(CNIEnv env) {
			return iterator.hasNext();
		}

		@Override
		public final CandyObject next(CNIEnv env) {
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
		public final boolean hasNext(CNIEnv env) {
			return i < size;
		}

		@Override
		public final CandyObject next(CNIEnv env) {
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
	public CandyObject getAttr(CNIEnv env, String attr) {
		switch (attr) {
			case Names.METHOD_ITERATOR_NEXT:
				return next;
			case Names.METHOD_ITERATOR_HAS_NEXT:
				return hasNext;
		}
		return super.getAttr(env, attr);
	}
	
	public final CandyObject hasNext(CNIEnv env, CandyObject[] args) {
		return BoolObj.valueOf(hasNext(env));
	}
	
	public final CandyObject next(CNIEnv env, CandyObject[] args) {
		return next(env);
	}
	
	public abstract boolean hasNext(CNIEnv env);
	public abstract CandyObject next(CNIEnv env);
}
