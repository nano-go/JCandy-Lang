package com.nano.candy.parser;

import org.junit.Test;

import static org.junit.Assert.*;
import static com.nano.candy.parser.TokenKind.*;

public class ScannerTest {
	
	@Test(timeout = 2000) public void testTokenKinds() {
		CandyTestCase.run(ScannerTestCase.TOK_STREAM_TEST_CASES) ;
	}
	
	@Test(timeout = 2000) public void testNumberLiteral() {
		CandyTestCase.run(ScannerTestCase.NUMBER_LITERAL_CASES) ;
	}
	
	@Test(timeout = 2000) public void testPositions() {
		CandyTestCase.run(ScannerTestCase.POSITION_AND_ERROR_TEST_CASES) ;
	}
	
}
