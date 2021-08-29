package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.error.InterruptedError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
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
	
	@NativeMethod(name = "synchronize", argc = 1)
	public CandyObject synchronize(CNIEnv env, CandyObject[] args) {
		CallableObj fn = TypeError.requiresCallable(args[0]);
		lock.lock();
		try {
			ObjectHelper.callFunction(env, fn);
		} finally {
			lock.unlock();
		}
		return null;
	}
	
	@NativeMethod(name = "lock")
	public CandyObject lock(CNIEnv env, CandyObject[] args) {
		lock.lock();
		return this;
	}
	
	@NativeMethod(name = "tryLock", argc = 1, varArgsIndex = 0)
	public CandyObject tryLock(CNIEnv env, CandyObject[] args) {
		CandyObject second = ObjectHelper.getOptionalArgument(args[0], IntegerObj.valueOf(0));
		long s = ObjectHelper.asInteger(second);
		try {
			return BoolObj.valueOf(lock.tryLock(s, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			new InterruptedError(e).throwSelfNative();
		}
		return null;
	}
	
	@NativeMethod(name = "unlock")
	public CandyObject ulock(CNIEnv env, CandyObject[] args) {
		lock.unlock();
		return this;
	}
	
	@NativeMethod(name = "getHoldCount")
	public CandyObject getHoldCount(CNIEnv env, CandyObject[] args) {
		return IntegerObj.valueOf(lock.getHoldCount());
	}
	
	@NativeMethod(name = "isHeldByCurrentThread")
	public CandyObject isHeldByCurrentThread(CNIEnv env, CandyObject[] args) {
		return BoolObj.valueOf(lock.isHeldByCurrentThread());
	}
	
	@NativeMethod(name = "isLocked")
	public CandyObject isLocked(CNIEnv env, CandyObject[] args) {
		return BoolObj.valueOf(lock.isLocked());
	}
	
	@NativeMethod(name = "isFair")
	public CandyObject isFair(CNIEnv env, CandyObject[] args) {
		return BoolObj.valueOf(lock.isFair());
	}
}
