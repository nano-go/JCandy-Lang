package com.nano.candy.ast.printer;

public class AstPrinters {

	public static final int PRINT_AST_IN_JSON_MASK = 1;
	
	public static AstPrinter newPrinter(int flag) {
		if ((flag & PRINT_AST_IN_JSON_MASK) != 0) {
			return new JsonAstPrinter();
		}
		throw new RuntimeException("Unknown ast printer flag: " + Integer.toBinaryString(flag));
	}   
}
