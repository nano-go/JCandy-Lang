package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.error.InterruptedError;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.builtin.utils.OptionalArg;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeMethod;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@NativeClass(name = "Lock", isInheritable = true)
public class LockObj extends CandyObject {
	
	public static final CandyClass LOCK_CLASS =
		NativeClassRegister.generateNativeClass(LockObj.class);
	
	private ReentrantLock lock;
	
	public LockObj() {
		super(LOCK_CLASS);
		lock = new ReentrantLock();
	}

	@Override
	public StringObj str(CNIEnv env) {
		return StringObj.valueOf(lock.toString());
	}
	
	@NativeMethod(name = "synchronize")
	public CandyObject synchronize(CNIEnv env, CallableObj fn) {
		lock.lock();
		try {
			ObjectHelper.callFunction(env, fn);
		} finally {
			lock.unlock();
		}
		return null;
	}
	
	@NativeMethod(name = "lock")
	public CandyObject lock(CNIEnv env) {
		lock.lock();
		return this;
	}
	
	@NativeMethod(name = "tryLock")
	public CandyObject tryLock(CNIEnv env, OptionalArg timeout) {
		long s = ObjectHelper.asInteger(timeout.getValue(0));
		try {
			return BoolObj.valueOf(lock.tryLock(s, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			new InterruptedError(e).throwSelfNative();
		}
		return null;
	}
	
	@NativeMethod(name = "unlock")
	public CandyObject ulock(CNIEnv env) {
		lock.unlock();
		return this;
	}
	
	@NativeMethod(name = "getHoldCount")
	public CandyObject getHoldCount(CNIEnv env) {
		return IntegerObj.valueOf(lock.getHoldCount());
	}
	
	@NativeMethod(name = "isHeldByCurrentThread")
	public CandyObject isHeldByCurrentThread(CNIEnv env) {
		return BoolObj.valueOf(lock.isHeldByCurrentThread());
	}
	
	@NativeMethod(name = "isLocked")
	public CandyObject isLocked(CNIEnv env) {
		return BoolObj.valueOf(lock.isLocked());
	}
	
	@NativeMethod(name = "isFair")
	public CandyObject isFair(CNIEnv env) {
		return BoolObj.valueOf(lock.isFair());
	}
}
