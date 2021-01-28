package com.nano.candy.parser;

import com.nano.candy.ast.Program;
import com.nano.candy.common.CandyTestCase;
import com.nano.candy.common.LoggerMsgChecker;
import com.nano.candy.parser.CandyParser;
import com.nano.candy.parser.Parser;
import com.nano.candy.parser.ScannerFactory;
import com.nano.candy.utils.Logger;
import org.junit.Test;

import static org.junit.Assert.*;

public class ParserTest {
	final Logger logger = Logger.getLogger() ;

	@Test public void testParser() {
		CandyTestCase.run(ParserTestCase.UNEXPECTED_ERROR_CASES) ;
	}

	@Test public void testErrors() {
		CandyTestCase.run(ParserTestCase.PARSER_ERROR_CASES) ;
	}

}
