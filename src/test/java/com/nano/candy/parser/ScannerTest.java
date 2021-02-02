package com.nano.candy.parser;

import com.nano.candy.common.CandyTestCase;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.nano.candy.parser.TokenKind.*;

public class ScannerTest {
	
	@Test(timeout = 2000) public void testTokenKinds() {
		CandyTestCase.run(ScannerTestCase.TK_TEST_CASES) ;
	}
	
	@Test(timeout = 2000) public void testPositions() {
		CandyTestCase.run(ScannerTestCase.PE_TEST_CASES) ;
	}
	
}
