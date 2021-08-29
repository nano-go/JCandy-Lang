package com.nano.candy.utils.objpool;
import java.lang.reflect.Array;

public class GenericObjectPool<R extends Recyclable> implements ObjectPool<R> {
	
	private R[] pool;
	private int p;
	
	public GenericObjectPool(int size, R[] arr) {
		this.pool = (R[]) Array.newInstance(
			arr.getClass().getComponentType(), size
		);
		this.p = 0;
	}
	
	@Override
	public R fetch() {
		if (p <= 0) {
			return null;
		}
		p--;
		R obj = pool[p];
		pool[p] = null;
		return obj;
	}

	@Override
	public void recycle(R obj) {
		obj.release();
		if (p < pool.length) {
			this.pool[p++] = obj;
		}
	}
	
}
