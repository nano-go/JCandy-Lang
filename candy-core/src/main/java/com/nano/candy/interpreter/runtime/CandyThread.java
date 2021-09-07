package com.nano.candy.interpreter.runtime;

import com.nano.candy.code.Chunk;
import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.BoolObj;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.IntegerObj;
import com.nano.candy.interpreter.builtin.type.ModuleObj;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.builtin.type.error.InterruptedError;
import com.nano.candy.interpreter.builtin.type.error.StateError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeFunc;
import com.nano.candy.interpreter.cni.NativeFuncRegister;
import com.nano.candy.interpreter.cni.NativeMethod;
import com.nano.candy.interpreter.runtime.CandyThread;
import com.nano.candy.std.Names;

import static com.nano.candy.interpreter.cni.JavaFunctionObj.*;

@NativeClass(name = "Thread", isInheritable = true)
public class CandyThread extends CandyObject {

	private static volatile int runningThreadCounter = 0;
	private static Object threadCounterLock = new Object();
	
	public static final CandyClass THREAD_CLASS = 
		NativeClassRegister.generateNativeClass(CandyThread.class) ;
		
	static {
		NativeFuncRegister.register(THREAD_CLASS, CandyThread.class); 
	}
	
	@NativeFunc(name = "waitOtherThreadsEnd")
	public static CandyObject waitOtherThreadsEnd(CNIEnv env, CandyObject[] args) {
		waitOtherThreadsEnd();
		return null;
	}
	
	@NativeFunc(name = "start", arity = 1)
	public static CandyObject startThread(CNIEnv env, CandyObject[] args) {
		CallableObj runner = TypeError.requiresCallable(args[0]);
		CandyThread t = new CandyThread(env, runner);
		t.start();
		return t;
	}
	
	@NativeFunc(name = "yield")
	public static CandyObject yield(CNIEnv env, CandyObject[] args) {
		Thread.yield();
		return null;
	}
	
	@NativeFunc(name = "sleep")
	public static CandyObject sleep(CNIEnv env, CandyObject[] args) {
		try {
			Thread.sleep(ObjectHelper.asInteger(args[0]));
		} catch (InterruptedException e) {
			new InterruptedError(e).throwSelfNative();
		} catch (IllegalArgumentException e) {
			new ArgumentError(e.getMessage()).throwSelfNative();
		}
		return null;
	}
	
	@NativeFunc(name = "current")
	public static CandyObject currentThread(CNIEnv env, CandyObject[] args) {
		return env.getCurrentThread();
	}
	
	public static void waitOtherThreadsEnd() {
		while (true) {		
			if (runningThreadCounter == 0) break;
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {}
		}
	}

	private static class InnerThread extends Thread {
		private Evaluator evaluator;
		private CallableObj target;

		public InnerThread(Evaluator evaluator, CallableObj target) {
			this.evaluator = evaluator;
			this.target = target;
		}

		@Override
		public void run() {		
			try {
				evaluator.eval(target, 0);
			} catch (VMExitException e) {
				// Means an error occurred.
				// We catch the exception to avoid printing unnecessary message.
			} finally {
				synchronized (threadCounterLock) {
					runningThreadCounter --;
				}
			}
		}
	}

	private Thread javaThread;
	protected EvaluatorEnv mEnv;

	protected CandyThread() {
		super(THREAD_CLASS);
	}

	public CandyThread(Thread javaThread, InterpreterOptions options) {
		super(THREAD_CLASS);
		setThread(javaThread);
		this.mEnv = new EvaluatorEnv(this, options);
	}
	
	public CandyThread(CNIEnv env, CallableObj runner) {
		super(THREAD_CLASS);
		init(env, runner);
	}

	private void init(CNIEnv env, CallableObj runner) {	
		this.mEnv = new EvaluatorEnv(this, env.getOptions());
		Thread javaThread = new InnerThread(mEnv.getEvaluator(), runner);
		setThread(javaThread);
	}

	private void setThread(Thread javaThread) {
		this.javaThread = javaThread;
		this.javaThread.setName("CandyThread - " + javaThread.getId());
		setMetaData("name", StringObj.valueOf(javaThread.getName()));
		setMetaData("id", IntegerObj.valueOf(javaThread.getId()));
		setMetaData("isDaemon", BoolObj.valueOf(javaThread.isDaemon()));
	}

	public Thread getJavaThread() {
		return javaThread;
	}

	public String getName() {
		return this.javaThread.getName();
	}

	public long getId() {
		return this.javaThread.getId();
	}
	
	public void start() {
		if (javaThread instanceof InnerThread) {
			synchronized (threadCounterLock) {
				runningThreadCounter ++;
			}
			this.javaThread.start();
		} else {
			throw new Error("Can't run an unrecognized java thread.");
		}
	}
	
	public ModuleObj run(Chunk chunk) {
		return mEnv.getEvaluator().eval(chunk);
	}

	@Override
	public CandyObject setAttr(CNIEnv env, String name, CandyObject value) {
		switch (name) {
			case "name":
				this.javaThread.setName(ObjectHelper.asString(value));
				break;
			case "isDaemon":
				this.javaThread.setDaemon(value.boolValue(env).value());
				break;
			case "id": case "isInterrupted": case "isAlive":
				AttributeError.throwReadOnlyError(name);
				break;
		}
		return super.setAttr(env, name, value);
	}

	@Override
	public CandyObject getAttr(CNIEnv env, String name) {
		switch(name){ 
			case "isInterrupted": 
				return BoolObj.valueOf(this.javaThread.isInterrupted());	
			case "isAlive":
				return BoolObj.valueOf(this.javaThread.isAlive());
		}
		return super.getAttr(env, name);
	}
	
	@NativeMethod(name = Names.METHOD_INITALIZER, arity = 1)
	public CandyObject init(CNIEnv env, CandyObject[] args) {
		CallableObj runner = TypeError.requiresCallable(args[0]);
		init(env, runner);
		return this;
	}
	
	@NativeMethod(name = "start")
	public CandyObject start(CNIEnv env, CandyObject[] args) {
		try {
			start();
		} catch (IllegalThreadStateException e) {
			new StateError("This thread '" + getName() + "' has already started.")
				.throwSelfNative();
		}
		return null;
	}
	
	@NativeMethod(name = "join")
	public CandyObject join(CNIEnv env, CandyObject[] args) {
		try {
			this.javaThread.join();
		} catch (InterruptedException e) {
			new InterruptedError(e).throwSelfNative();
		}
		return null;
	}
	
	@NativeMethod(name = "interrupt")
	public CandyObject interrupt(CNIEnv env, CandyObject[] args) {
		this.javaThread.interrupt();
		return null;
	}
}

