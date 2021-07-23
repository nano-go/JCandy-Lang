package com.nano.candy.interpreter.i2.runtime;

import com.nano.candy.sys.CandySystem;

public class CandyThread {
	
	private Thread javaThread;
	
	/**
	 * Each of thread has a stack used to store the frames produced by
	 * the method.
	 */
	private StackFrame stack;
	
	public CandyThread() {
		this(Thread.currentThread());
	}
	
	public CandyThread(Thread javaThread) {
		this.javaThread = javaThread;
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
	
	public final StackFrame getCandyStack() {
		return stack;
	}
	
	public final void pushFrame(Frame frame) {
		stack.pushFrame(frame);
	}
	
	public final Frame popFrame() {
		return stack.popFrame();
	}
}
