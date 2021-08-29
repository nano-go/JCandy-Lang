package com.nano.candy.code;

public class CodeAttribute {
	
	public int fromPc;
	public int length;
	public int maxStack;
	public int maxLocal;
	public ErrorHandlerTable errorHandlerTable;

	public CodeAttribute(int fromPc, int length, int maxStack, int maxLocal, 
						 ErrorHandlerTable errorHandlerTable) {
		this.fromPc = fromPc;
		this.length = length;
		this.maxStack = maxStack;
		this.maxLocal = maxLocal;
		this.errorHandlerTable = errorHandlerTable;
	}
}
