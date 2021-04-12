package com.nano.candy.interpreter.i2.rtda;
import com.nano.candy.interpreter.i2.builtin.CandyObject;

public interface OperandStack {
	
	public CandyObject pop();
	public CandyObject peek(int k);
	public void push(CandyObject operand);
	
	/**
	 * Swaps the two-stack operands.
	 */
	public void swap();
	
	/**
	 * Lifts the second and third operands one position up, moves top
	 * operand down position three.
	 */
	public void rotThree();
	
	public int size();
	public void clear();
}
