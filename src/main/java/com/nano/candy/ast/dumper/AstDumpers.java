package com.nano.candy.ast.dumper;

public class AstDumpers {
	
    public static final int JSON_MASK = 1;

	public static AstDumper newPrinter(int flag) {
		if ((flag & JSON_MASK) != 0) {
			return new JsonAstDumper();
		}
		throw new RuntimeException("Unknown ast dumper flag: " + Integer.toBinaryString(flag));
	}   
}
