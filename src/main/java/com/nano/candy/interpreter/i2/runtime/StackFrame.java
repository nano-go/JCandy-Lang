package com.nano.candy.interpreter.i2.runtime;

import com.nano.candy.interpreter.i2.builtin.type.error.StackOverflowError;
import java.util.Arrays;

public final class StackFrame {

	private Frame[] stack;
	private int sp;
	private Frame frame;

	private final int maxStackDeepth;
	
	public StackFrame(int maxStackDeepth) {
		this.maxStackDeepth = maxStackDeepth;
		this.sp = 1;
		this.stack = new Frame[16];
		this.frame = null;
	}
	
	public int sp() {
		return sp-1;
	}
	
	public int frameCount() {
		return sp-1;
	}
	
	public boolean isEmpty() {
		return frameCount() == 0;
	}
	
	public Frame getAt(int index) {
		return stack[index + 1];
	}
	
	public Frame peek(int k) {
		return stack[sp-k-1];
	}
	
	public Frame peek() {
		return frame;
	}
	
	public void pushFrame(Frame frame) {
		if (sp > maxStackDeepth) {
			new StackOverflowError(maxStackDeepth).throwSelfNative();
		}
		if (sp >= stack.length) {
			stack = Arrays.copyOf(stack, stack.length*2);
		}
		this.frame = frame;
		this.stack[sp ++] = frame;
	}
	
	public Frame popFrame() {
		Frame old = this.stack[-- sp];
		this.frame = this.stack[sp - 1];
		return old;
	}
	
	public void clearFrame() {
		for (int i = 1; i < sp; i ++) {
			stack[i].closeAllUpvalues();
			stack[i] = null;
		}
		this.sp = 1;
	}
}
