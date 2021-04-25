package com.nano.candy.interpreter.i2.builtin.type;
import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.IteratorObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.ObjectClass;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.rtda.Variable;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import java.util.Iterator;
import java.util.Map;

@NativeClass(name = "Iterator")
public abstract class IteratorObj extends BuiltinObject {
	
	public static class MapIterator extends IteratorObj {
		private Iterator<Map.Entry<String, CandyObject>> iterator;
		
		public MapIterator(Iterator<Map.Entry<String, CandyObject>> iterator) {
			this.iterator = iterator;
		}
		@Override
		public boolean hasNext(VM vm) {
			return iterator.hasNext();
		}

		@Override
		public CandyObject next(VM vm) {
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
		public boolean hasNext(VM vm) {
			return iterator.hasNext();
		}

		@Override
		public CandyObject next(VM vm) {
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
		public boolean hasNext(VM vm) {
			return i < size;
		}

		@Override
		public CandyObject next(VM vm) {
			return elements[i ++];
		}
	}
	
	private static final CandyClass ITERATOR_CLASS = 
		NativeClassRegister.generateNativeClass(IteratorObj.class);
	public IteratorObj() {
		super(ITERATOR_CLASS);
	}
	
	@NativeMethod(name = Names.METHOD_ITERATOR_HAS_NEXT)
	private CandyObject hasNext(VM vm, CandyObject... args) {
		return BoolObj.valueOf(hasNext(vm));
	}
	
	@NativeMethod(name = Names.METHOD_ITERATOR_NEXT)
	private CandyObject next(VM vm, CandyObject... args) {
		return next(vm);
	}
	
	public abstract boolean hasNext(VM vm);
	public abstract CandyObject next(VM vm);
}
