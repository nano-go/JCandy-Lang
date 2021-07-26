package com.nano.candy.interpreter.i2.runtime;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.builtin.type.error.InterruptedError;
import com.nano.candy.interpreter.i2.builtin.type.error.StateError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.runtime.CandyThread;
import com.nano.candy.std.Names;
import com.nano.candy.sys.CandySystem;

import static com.nano.candy.interpreter.i2.cni.FasterNativeMethod.*;

@NativeClass(name = "Thread", isInheritable = true)
public class CandyThread extends CandyObject {

	private static volatile int runningThreadCounter = 0;
	private static Object threadCounterLock = new Object();
	
	public static final CandyClass THREAD_CLASS = 
		NativeClassRegister.generateNativeClass(CandyThread.class) ;
		
	static {
		addStatic(THREAD_CLASS, "waitOtherThreadsEnd", 0, 
			CandyThread::waitOtherThreadsEnd);
		addStatic(THREAD_CLASS, "start", 1, CandyThread::startThread);	
		addStatic(THREAD_CLASS, "sleep", 1, CandyThread::sleep);
		addStatic(THREAD_CLASS, "yield", 0, CandyThread::yield);
		addStatic(THREAD_CLASS, "current", 0, CandyThread::currentThread);
	}
	
	public static CandyObject waitOtherThreadsEnd(CNIEnv env, CandyObject[] args) {
		waitOtherThreadsEnd();
		return null;
	}
	
	public static CandyObject startThread(CNIEnv env, CandyObject[] args) {
		CallableObj runner = TypeError.requiresCallable(args[0]);
		CandyThread t = new CandyThread(env, runner);
		t.getJavaThread().start();
		return t;
	}
	
	public static CandyObject yield(CNIEnv env, CandyObject[] args) {
		Thread.yield();
		return null;
	}
	
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
	
	public static CandyObject currentThread(CNIEnv env, CandyObject[] args) {
		return env.getCurrentThread();
	}
	
	public static void waitOtherThreadsEnd() {
		while (true) {
			if (runningThreadCounter == 0) break;
			Thread.yield();
		}
	}

	private static class InnerThread extends Thread {
		private CandyThread candyThread;
		private EvaluatorEnv env;
		private CallableObj target;

		public InnerThread(CandyThread candyThread, EvaluatorEnv env, CallableObj target) {
			this.candyThread = candyThread;
			this.env = env;
			this.target = target;
		}

		@Override
		public void run() {
			synchronized (threadCounterLock) {
				runningThreadCounter ++;
			}
			EvaluatorEnv newEnv = new EvaluatorEnv(candyThread, env.getOptions());
			try {
				newEnv.getEvaluator().eval(target, 0);
			} catch (VMExitException e) {
				// Means an error occurred.
				// We catch the exception to avoid printing unnecessary prompts.
			} finally {
				synchronized (threadCounterLock) {
					runningThreadCounter --;
				}
			}
		}
	}

	private Thread javaThread;

	/**
	 * Each of thread has a stack used to store the frames produced by
	 * the method.
	 */
	protected StackFrame stack;

	protected CandyThread() {
		super(THREAD_CLASS);
	}

	protected CandyThread(Thread javaThread) {
		super(THREAD_CLASS);
		this.stack = new StackFrame(CandySystem.DEFAULT_MAX_STACK);
		setThread(javaThread);
	}
	
	public CandyThread(CNIEnv env, CallableObj runner) {
		super(THREAD_CLASS);
		init(env, runner);
	}

	private void init(CNIEnv env, CallableObj runner) {	
		this.stack = new StackFrame(CandySystem.DEFAULT_MAX_STACK);
		Thread javaThread = new InnerThread(this, env.getEvaluatorEnv(), runner);
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

	public Frame[] getStack() {
		Frame[] frames = new Frame[stack.sp()];
		for (int i = 0; i < frames.length; i ++) {
			frames[i] = stack.getAt(stack.sp()-i-1);
		}
		return frames;
	}

	protected final void pushFrame(Frame frame) {
		stack.pushFrame(frame);
	}

	protected final Frame popFrame() {
		return stack.popFrame();
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
	
	@NativeMethod(name = Names.METHOD_INITALIZER, argc = 1)
	public CandyObject init(CNIEnv env, CandyObject[] args) {
		CallableObj runner = TypeError.requiresCallable(args[0]);
		init(env, runner);
		return this;
	}
	
	@NativeMethod(name = "start")
	public CandyObject start(CNIEnv env, CandyObject[] args) {
		try {
			this.javaThread.start();
		} catch (IllegalThreadStateException e) {
			new StateError("This thread '" + getName() + "' has already started.");
		}
		return null;
	}
	
	@NativeMethod(name = "join")
	public CandyObject join(CNIEnv env, CandyObject[] args) {
		try {
			this.javaThread.join();
		} catch (InterruptedException e) {
			new InterruptedError(e);
		}
		return null;
	}
	
	@NativeMethod(name = "interrupt")
	public CandyObject interrupt(CNIEnv env, CandyObject[] args) {
		this.javaThread.interrupt();
		return null;
	}
}

