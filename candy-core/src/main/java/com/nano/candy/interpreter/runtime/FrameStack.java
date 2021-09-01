package com.nano.candy.interpreter.runtime;

import com.nano.candy.interpreter.builtin.type.error.StackOverflowError;
import java.util.Arrays;

public final class FrameStack {

	private Frame[] stack;
	private int sp;
	
	private final int maxStackDeepth;
	
	public FrameStack(int maxStackDeepth) {
		this.maxStackDeepth = maxStackDeepth;
		this.sp = 1;
		this.stack = new Frame[16];
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
		return peek(0);
	}
	
	public void pushFrame(Frame frame) {
		if (sp > maxStackDeepth) {
			new StackOverflowError(maxStackDeepth).throwSelfNative();
		}
		if (sp >= stack.length) {
			stack = Arrays.copyOf(stack, stack.length*2);
		}
		this.stack[sp ++] = frame;
	}
	
	public Frame popFrame() {
		return this.stack[-- sp];
	}
	
	public void clearFrame() {
		for (int i = 1; i < sp; i ++) {
			stack[i].closeAllUpvalues();
			stack[i] = null;
		}
		this.sp = 1;
	}
}
