package com.nano.candy.interpreter.i2.runtime;

import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.sys.CandySystem;

public class CandyThread {
	
	private static int threadCounter = 0;
	private static Object threadCounterLock = new Object();
	
	public static void waitOtherThreadsEnd() {
		while (true) {
			synchronized(threadCounterLock) {
				if (threadCounter == 0) break;
			}
			Thread.yield();
		}
	}
	
	public static CandyThread startToRunCallableObj(EvaluatorEnv env, 
	                                                CallableObj target) {
		InnerThread t = new InnerThread();
		CandyThread candyThread = new CandyThread(t);
		t.setTarget(() -> {		
			EvaluatorEnv newEnv = new EvaluatorEnv(candyThread, env.getOptions());
			try {
				newEnv.getEvaluator().eval(target, 0);
			} catch (VMExitException e) {
				// Means an error occurred.
				// We catch the exception to avoid printing unnecessary prompts.
			} finally {
				synchronized(threadCounterLock) {
					threadCounter --;
				}
			}
		});
		synchronized(threadCounterLock) {
			threadCounter ++;
		}
		t.start();
		return candyThread;
	}
	
	private static class InnerThread extends Thread {
		private Runnable target;

		public InnerThread() {
		}
		
		public void setTarget(Runnable target) {
			this.target = target;
		}
		
		@Override
		public void run() {
			if (target != null) {
				target.run();
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
		this(Thread.currentThread());
	}
	
	protected CandyThread(Thread javaThread) {
		this.javaThread = javaThread;
		this.javaThread.setName("CandyThread - " + javaThread.getId());
		this.stack = new StackFrame(CandySystem.DEFAULT_MAX_STACK);
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
}
