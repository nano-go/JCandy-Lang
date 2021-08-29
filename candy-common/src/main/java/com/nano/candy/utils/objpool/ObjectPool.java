package com.nano.candy.utils.objpool;

public interface ObjectPool<R extends Recyclable> {
	public R fetch();
	public void recycle(R obj);
}
