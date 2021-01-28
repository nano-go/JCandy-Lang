package com.nano.candy.interpreter.i1;

import com.nano.candy.interpreter.i1.builtin.CandyObject;

public class ControlFlowException extends RuntimeException {
	
	public static class ReturnException extends ControlFlowException{
		public CandyObject returnedObj ;

		public ReturnException(CandyObject returnedObj) {
			this.returnedObj = returnedObj;
		}
	}
	
	public static class BreakException extends ControlFlowException{}
	public static class ContinueException extends ControlFlowException{}
	
}
