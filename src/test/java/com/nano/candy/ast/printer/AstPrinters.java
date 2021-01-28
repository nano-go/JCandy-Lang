package com.nano.candy.ast.printer;

import com.nano.candy.main.CandyOptions;

public class AstPrinters {

	public static AstPrinter newPrinter(CandyOptions options) {
		int flag = options.getPrintAstFlag() ;
		if ((flag & CandyOptions.PRINT_AST_BY_JSON_MASK) != 0) {
			return new JsonAstPrinter() ;
		}
		throw new RuntimeException("Unknown ast printer flag: " + Integer.toBinaryString(flag)) ;
	}   
}
