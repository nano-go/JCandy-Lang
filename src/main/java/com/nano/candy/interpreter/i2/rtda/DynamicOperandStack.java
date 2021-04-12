package com.nano.candy.interpreter.i2.rtda;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.utils.ArrayUtils;

public final class DynamicOperandStack implements OperandStack {
	
	private CandyObject[] opStack;
	private int sp;

	public DynamicOperandStack(int slots) {
		opStack = new CandyObject[Math.max(4, slots)];
	}

	@Override
	public void rotThree() {
		int sp = this.sp-1;
		CandyObject top = opStack[sp];
		opStack[sp] = opStack[sp-1];
		opStack[sp-1] = opStack[sp-2];
		opStack[sp-2] = top;
	}

	@Override
	public void swap() {
		CandyObject tmp = opStack[sp-1];
		opStack[sp-1] = opStack[sp-2];
		opStack[sp-2] = tmp;
	}

	@Override
	public CandyObject pop() {
		return opStack[-- sp];
	}

	@Override
	public CandyObject peek(int k) {
		return opStack[sp - k - 1];
	}

	@Override
	public void push(CandyObject operand) {
		opStack = ArrayUtils.growCapacity(opStack, sp);
		opStack[sp ++] = operand;
	}

	@Override
	public int size() {
		return sp;
	}

	@Override
	public void clear() {
		for (int i = 0; i < sp; i ++) {
			opStack[i] = null;
		}
		sp = 0;
	}
}
