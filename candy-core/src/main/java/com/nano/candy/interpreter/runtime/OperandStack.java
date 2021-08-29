package com.nano.candy.interpreter.runtime;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import java.util.Arrays;

public class OperandStack {
	
	protected CandyObject[] operands;
	protected int sp;
	
	public OperandStack(int initialCapacity) {
		this.operands = new CandyObject[initialCapacity];
		this.sp = 1;
	}
	
	public final void reverse(int n) {
		final int hn = n >> 1;
		for(int i = 0; i < hn; i ++) {
			CandyObject tmp = operands[sp-n+i];
			operands[sp-n+i] = operands[sp-i-1];
			operands[sp-i-1] = tmp;
		}
	}
	
	public final void rotThree() {
		int sp = this.sp-1;
		CandyObject top = operands[sp];
		operands[sp] = operands[sp-1];
		operands[sp-1] = operands[sp-2];
		operands[sp-2] = top;
	}
	
	public final void swap() {
		CandyObject tmp = operands[sp-1];
		operands[sp-1] = operands[sp-2];
		operands[sp-2] = tmp;
	}
	
	public final CandyObject pop() {
		return operands[-- sp];
	}
	
	public final CandyObject peek(int k) {
		return operands[sp - k - 1];
	}
	
	public final void pushArguments(CandyObject... args) {
		for (CandyObject arg : args) {
			if (arg == null) {
				push(NullPointer.nil());
			} else {
				push(arg);
			}
		}
	}
	
	public final void push(CandyObject operand) {
		operands[sp ++] = operand;
	}
	
	public final int size() {
		return sp;
	}
	
	public final void pop(int bp) {
		for (int i = bp; i < this.sp; i ++) {
			operands[i] = null;
		} 
		this.sp = bp;
	}
	
	protected final void push(int frameSize) {
		if (this.sp + frameSize >= operands.length) {
			int minSize = this.sp + frameSize;
			int len = operands.length;
			do {
				len *= 1.5;
			} while (len <= minSize);
			this.operands = Arrays.copyOf(this.operands, len);
		}
	}
	
	protected final void clearOperands(int bp) {
		for (;bp < sp; bp ++) {
			operands[bp] = null;
		}
	}
	
	public String toString(int bp, int local) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int i = 0; i < bp; i ++) {
			builder.append(operands[i]);
			builder.append(", ");
		}
		builder.append("] [");
		for (int i = bp; i < bp + local; i ++) {
			builder.append(operands[i]);
			builder.append(", ");
		}
		builder.append("] [");
		for (int i = bp + local; i < sp; i ++) {
			builder.append(operands[i]);
			builder.append(", ");
		}
		builder.append("]");
		return builder.toString();
	}
}
