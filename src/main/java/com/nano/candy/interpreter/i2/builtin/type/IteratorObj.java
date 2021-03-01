package com.nano.candy.interpreter.i2.builtin.type;
import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.IteratorObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.ObjectClass;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;

public abstract class IteratorObj extends BuiltinObject {
	
	private CandyObject next;
	private CandyObject hasNext;
	
	public IteratorObj() {
		super(ObjectClass.getObjClass());
		this.next = genNextMethod();
		this.hasNext = genHasNextMethod();
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

	private CandyObject genNextMethod() {
		return ObjectHelper.genMethod(this,
			Names.METHOD_ITERATOR_NEXT, 0, IteratorObj::next);
	}
	
	private CandyObject genHasNextMethod() {
		return ObjectHelper.genMethod(this,
			Names.METHOD_ITERATOR_HAS_NEXT,0, IteratorObj::hasNext);
	}
	
	private static CandyObject hasNext(CandyObject iterator, VM vm) {
		return BoolObj.valueOf(((IteratorObj)iterator).hasNext(vm));
	}

	private static CandyObject next(CandyObject iterator, VM vm) {
		return ((IteratorObj)iterator).next(vm);
	}
	
	public abstract boolean hasNext(VM vm);
	public abstract CandyObject next(VM vm);
}
