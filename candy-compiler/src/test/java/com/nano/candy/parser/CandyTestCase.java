package com.nano.candy.parser;

public interface CandyTestCase {
	public void assertCase() ;   
	
	public static void run(CandyTestCase[] testCases) {
		for (CandyTestCase c : testCases) {
			c.assertCase() ;
		}
	}
}
