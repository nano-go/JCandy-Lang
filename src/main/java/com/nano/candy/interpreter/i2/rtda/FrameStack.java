package com.nano.candy.interpreter.i2.rtda;

import java.util.Arrays;

public final class FrameStack {

	private Frame[] frameStack;
	private int sp;
	private Frame frame;

	private final int maxStackDeepth;
	
	public FrameStack(int maxStackDeepth) {
		this.maxStackDeepth = maxStackDeepth;
		this.sp = 1;
		this.frameStack = new Frame[16];
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
		return frameStack[index + 1];
	}
	
	public Frame peek() {
		return frame;
	}
	
	public void pushFrame(Frame frame) {
		if (sp > maxStackDeepth) {
			throw new StackOverflowError();
		}
		if (sp >= frameStack.length) {
			frameStack = Arrays.copyOf(frameStack, frameStack.length*2);
		}
		this.frame = frame;
		this.frameStack[sp ++] = frame;
	}
	
	public Frame popFrame() {
		Frame old = this.frameStack[-- sp];
		this.frame = this.frameStack[sp - 1];
		return old;
	}
	
	public void clearFrame() {
		for (int i = 1; i < sp; i ++) {
			frameStack[i].release();
			frameStack[i] = null;
		}
		this.sp = 1;
	}
}
