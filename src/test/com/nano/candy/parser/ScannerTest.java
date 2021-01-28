package com.nano.candy.parser;

import com.nano.candy.common.CandyTestCase;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.nano.candy.parser.TokenKind.*;

public class ScannerTest {
	
	@Test public void testTokenKinds() {
		CandyTestCase.run(ScannerTestCase.TK_TEST_CASES) ;
	}
	
	@Test public void testPositions() {
		CandyTestCase.run(ScannerTestCase.PE_TEST_CASES) ;
	}
	
}
