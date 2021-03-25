package com.nano.candy.parser;

import com.nano.candy.common.CandyTestCase;
import com.nano.candy.common.LoggerMsgChecker;
import com.nano.candy.parser.Parser;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Position;
import java.util.ArrayList;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.nano.candy.parser.SimulationPositions.loc;

public class ParserTest {
	static final Logger logger = Logger.getLogger() ;

	public static final String[] UNEXPECTED_ERROR_CASES = {
		"var a = 5; a += 15; a -= 15; a *= 15; a /= 15; a %= 15",
		"var arr = [1, 5 + 6, lambda a,b -> 9, c] + 123 * 5",
		"var arr = [1, 5 + 6, a = 15, false]",
		"var arr = [1, 5 + 6, a = lambda a -> b]",
		"var arr = [[c, cpp, cs, lambda lisp -> lisp], id, name, java]",
		"var arr = [ \n[1, 2, 3, 4],\n [5,6,7,8],\n[9, 10, 11, 12]\n]",
		"var arr = [ \n[1, 2, 3, 4],\n [5,6,7,8],\n[9, 10, 11, 12],\n]",
		"[]",
		"if (false) var a = 0\nelse if (true) var b = 155 ; else if (a + b * c) true",
		"if (false) var a = 0\nelse var b = 155",
		"if (false) \n return true",
		"if (false) { return true; }",
		"if (false) \n { return true; } else if (a > b) \n { return false; }",
		"if (false) { return true; } else if (a > b) \n return false; ",
		"while (true) var a = b",
		"while (true) break",
		"while (true) \n break",
		"while (true) { break; }",
		"while (false) \n {var a = 66\nif (a) \n { break ; }}",
		"for (i in range(0, 7)) break",
		"for (i in b) { b; }",
		"for (i in b) \n b;",
		"for (i in b) \n { b; }",
		"for (i in a = lambda a,b -> a*b) {\nprintln(i);}",
		"fun test(a, b, c) { return 0 ; }",
		"fun test(a, b, c) { a * b * c ; }",
		"fun test(a, b, c) \n { a * b * c ; }",
		"test(lambda a, b -> if (a > b) b else c)",
		"test(lambda a, b -> if (a > b) b else return c)",
		"var lambdaExpr = lambda a, b -> a * b",
		"var lambdaExpr = lambda a, b -> (a * b)",
		"var lambdaExpr = lambda a, b -> { a * b\n }",
		"var lambdaExpr = lambda a, b -> {}",
		"var lambdaExpr = lambda a, b -> return b",
		"var a = lambda a -> return lambda a -> a",
		"a.b.c = 1566 * 55\n manager.getUser(5).getId = lambda -> return user.id",
		"stream()\n\t.filter(a).map(b).\nlimit(5)\n.skip(15)\n",
		"_call(a, lambda a -> a)",
		"_call(lambda a -> a)",
		"class Point {\n x() { \n return x; } y() { \n return y; } }",
		"class P {\n P() {this.x = x; this.y = y;} A() { super.test().b; } }",
		"class A \n {}",
		"class A \n { fun a(a, b) \n {} fun a() \n {} fun a(a, b) {} }",
		"",
	};
	
	@Test public void unexpectedErrorsTest() {
		for (String input : UNEXPECTED_ERROR_CASES) {
			Parser parser = ParserFactory.newParser("unexpected_error.cd", input) ;
			parser.parse() ;
			LoggerMsgChecker.unexpectedErrors(true, true) ;
		}
	}
	
	
	/***********************************************************************
	 * Parser Error Test
	 ***********************************************************************/
	
	public static final ParserErrorCase[] PARSER_ERROR_CASES = {
		newPECase("var a += 5", loc(1, 7), loc(1, 7)),
		newPECase("(a + b * 2", loc(1, 11)),
		newPECase("((a + \nb / c)", loc(2, 7)),
		newPECase("fun a(ff,,)", loc(1, 10), loc(1, 10)),
		newPECase("var a =\n\tlambda => {\n\treturn true\n}", loc(2, 9)),
		newPECase("var ", loc(1, 5)),
		newPECase("a. ", loc(1, 4)),
		newPECase("fun ", loc(1, 5)),
		newPECase("class ", loc(1, 7)),
		newPECase("class a :", loc(1, 10))
	};
	 
	public static ParserErrorCase newPECase(String input, SimulationPositions.Location... expectedLocations) {
		return new ParserErrorCase(input, expectedLocations) ;
	}
	
	private static class ParserErrorCase {
		SimulationPositions expected ;
		ArrayList<Position> actualPos ;

		public ParserErrorCase(String input, SimulationPositions.Location... expected) {
			this.expected = new SimulationPositions(input, expected) ;
			this.actualPos = new ArrayList<>() ;
		}
		public void assertErrors() {
			Parser parser = new CandyParser(ScannerFactory.newScanner("test.cd", expected.input)) ;
			parser.parse() ;
			for (Logger.LogMessage msg : logger.getErrorMessages()) {
				actualPos.add(msg.getPos()) ;
			}		
			LoggerMsgChecker.expectedErrors(true, true) ;
			assertArrayEquals(expected.positions, actualPos.toArray()) ;
		}
	}
	
	@Test public void testErrors() {
		for (ParserErrorCase pe : PARSER_ERROR_CASES) {
			pe.assertErrors();
		}
	}

}
