package com.nano.candy.code;

public class ErrorHandlerTable {

	private ErrorHandler[] table;
	public ErrorHandlerTable(ErrorHandler[] table) {
		this.table = table;
	}
	
	public int length() {
		return table.length;
	}
	
	public ErrorHandler get(int index) {
		return table[index];
	}

	public ErrorHandler findExceptionHandler(int pc) {
		for (int i = table.length-1; i >= 0; i--) {
			if (pc >= table[i].startPc && pc <= table[i].endPc) {
				return table[i];
			}
		}
		return null;
	}
	
	public static class ErrorHandler {
		public final int startPc;
		public final int endPc;
		public final int handlerPc;

		public ErrorHandler(int startPc, int endPc, int handlerPc) {
			this.startPc = startPc;
			this.endPc = endPc;
			this.handlerPc = handlerPc;
		}
	}
}
