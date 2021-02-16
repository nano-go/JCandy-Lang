package com.nano.candy.parser;

import com.nano.candy.common.CandyTestCase;
import com.nano.candy.common.LoggerMsgChecker;
import com.nano.candy.parser.SimulationPositions;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Position;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static com.nano.candy.parser.SimulationPositions.loc;

public class ParserTestCase {
	
	static final Logger logger = Logger.getLogger() ;
	
	public static CandyTestCase[] UNEXPECTED_ERROR_CASES = {
		newUECase("var a = 5; a += 15; a -= 15; a *= 15; a /= 15; a %= 15"),
		newUECase("var arr = [1, 5 + 6, lambda a,b -> 9, c] + 123 * 5"),
		newUECase("var arr = [1, 5 + 6, a = 15, false]"),
		newUECase("var arr = [1, 5 + 6, a = lambda a -> b]"),
		newUECase("var arr = [[c, cpp, cs, lambda lisp -> lisp], id, name, java]"),
		newUECase("[]"),
		newUECase("if (false) var a = 0\nelse if (true) var b = 155 ; else if (a + b * c) true"),
		newUECase("if (false) var a = 0\nelse var b = 155"),
		newUECase("if (false) \n return true"),
		newUECase("if (false) { return true; }"),
		newUECase("if (false) \n { return true; } else if (a > b) \n { return false; }"),
		newUECase("if (false) { return true; } else if (a > b) \n return false; "),
		newUECase("while (true) var a = b"),
		newUECase("while (true) break"),
		newUECase("while (true) \n break"),
		newUECase("while (true) { break; }"),
		newUECase("while (false) \n {var a = 66\nif (a) \n { break ; }}"),
		newUECase("for (i in range(0, 7)) break"),
		newUECase("for (i in b) { b; }"),
		newUECase("for (i in b) \n b;"),
		newUECase("for (i in b) \n { b; }"),
		newUECase("for (i in a = lambda a,b -> a*b) {\nprintln(i);}"),
		newUECase("fun test(a, b, c) { return 0 ; }"),
		newUECase("fun test(a, b, c) { a * b * c ; }"),
		newUECase("fun test(a, b, c) \n { a * b * c ; }"),
		newUECase("test(lambda a, b -> if (a > b) b else c)"),
		newUECase("test(lambda a, b -> if (a > b) b else return c)"),
		newUECase("var lambdaExpr = lambda a, b -> a * b"),
		newUECase("var lambdaExpr = lambda a, b -> (a * b)"),
		newUECase("var lambdaExpr = lambda a, b -> { a * b\n }"),
		newUECase("var lambdaExpr = lambda a, b -> {}"),
		newUECase("var lambdaExpr = lambda a, b -> return b"),
		newUECase("var a = lambda a -> return lambda a -> a"),
		newUECase("a.b.c = 1566 * 55\n manager.getUser(5).getId = lambda -> return user.id"),
		newUECase("_call(a, lambda a -> a)"),
		newUECase("_call(lambda a -> a)"),
		newUECase("class Point {\n x() { \n return x; } y() { \n return y; } }"),
		newUECase("class P {\n P() {this.x = x; this.y = y;} A() { super.test().b; } }"),
		newUECase("class A \n {}"),
		newUECase("class A \n { fun a(a, b) \n {} fun a() \n {} fun a(a, b) {} }"),
		newUECase(""),
	} ;
	
	private static UnexpectedErrorCase newUECase(String src) {
		return new UnexpectedErrorCase(src) ;
	}
	
	private static class UnexpectedErrorCase implements CandyTestCase {
		private String input ;

		public UnexpectedErrorCase(String input) {
			this.input = input;
		}
		@Override
		public void assertCase() {
			Parser parser = ParserFactory.newParser("unexpected_error.cd", input) ;
			parser.parse() ;
			LoggerMsgChecker.shouldNotAppearErrors(true, true) ;
		}
	}
	
	public static CandyTestCase[] PARSER_ERROR_CASES = {
		newETCase("var a += 5", loc(1, 7), loc(1, 7)),
		newETCase("(a + b * 2", loc(1, 11)),
		// newETCase("while (true)\n{break;}\nbreak", loc(3, 1)),
		// newETCase("while (true)\n{while(true){} break;}\n{if (true) }", loc(3, 2))
	} ;
	
	public static ErrorTestCase newETCase(String input, SimulationPositions.Location... expectedLocations) {
		return new ErrorTestCase(input, expectedLocations) ;
	}
	
	public static class ErrorTestCase implements CandyTestCase {

		SimulationPositions expected ;
		ArrayList<Position> actualPos ;
		
		public ErrorTestCase(String input, SimulationPositions.Location... expected) {
			this.expected = new SimulationPositions(input, expected) ;
			actualPos = new ArrayList<>() ;
		}	
		
		@Override
		public void assertCase() {
			Parser parser = new CandyParser(ScannerFactory.newScanner("test.cd", expected.input)) ;
			parser.parse() ;
			if (expected.positions.length == 0) {
				LoggerMsgChecker.shouldNotAppearErrors(true, true) ;
				return ;
			}
			for (Logger.LogMessage msg : logger.getErrorMessages()) {
				actualPos.add(msg.getPos()) ;
			}
			System.out.println(expected.input) ;
			LoggerMsgChecker.shouldAppearErrors(true, true) ;
			assertArrayEquals(expected.positions, actualPos.toArray()) ;
		}
		
	}
	
}
