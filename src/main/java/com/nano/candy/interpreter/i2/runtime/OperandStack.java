package com.nano.candy.interpreter.i2.runtime;
import com.nano.candy.interpreter.i2.builtin.CandyObject;

public class OperandStack {
	
	private CandyObject[] opStack;
	private int sp;

	public OperandStack(int slots) {
		opStack = new CandyObject[slots];
	}
	
	public final void rotThree() {
		int sp = this.sp-1;
		CandyObject top = opStack[sp];
		opStack[sp] = opStack[sp-1];
		opStack[sp-1] = opStack[sp-2];
		opStack[sp-2] = top;
	}
	
	public final void swap() {
		CandyObject tmp = opStack[sp-1];
		opStack[sp-1] = opStack[sp-2];
		opStack[sp-2] = tmp;
	}
	
	public final CandyObject pop() {
		return opStack[-- sp];
	}
	
	public final CandyObject peek(int k) {
		return opStack[sp - k - 1];
	}
	
	public final void push(CandyObject operand) {
		opStack[sp ++] = operand;
	}
	
	public final int size() {
		return sp;
	}
	
	public void clear() {
		for (int i = 0; i < sp; i ++) {
			opStack[i] = null;
		}
		sp = 0;
	}
}
