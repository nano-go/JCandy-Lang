package com.nano.candy.parser;

import com.nano.candy.common.CandyTestCase;
import com.nano.candy.config.Config;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Position;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.nano.candy.parser.TokenKind.*;
import static org.junit.Assert.*;
import com.nano.candy.common.LoggerMsgChecker;

public class ScannerTestCase {
	
	private static final TokenKind ID = TokenKind.IDENTIFIER ;
	private static final Token SEMIEOF= tk(TokenKind.SEMI, "EOF") ;
	private static final Token SEMILN= tk(TokenKind.SEMI, Config.END_OF_LINE) ;
	
	static final Logger logger = Logger.getLogger() ;
	
	static final CandyTestCase[] TK_TEST_CASES = {
		newTKCase("Test__112344__Abc // comment...\n", tk(ID, "Test__112344__Abc"), SEMILN),
		newTKCase("_abC // comment...", tk(ID, "_abC"), SEMIEOF),
		newTKCase(" _123bba /** comment...\nabcd*/\n", tk(ID, "_123bba"), SEMILN),
		newTKCase("_abC /** comment...*/", tk(ID, "_abC"), SEMIEOF),
		newTKCase("\n\t\t_abc 123 \n", tk(ID, "_abc"), tk(INTEGER, "123"), SEMILN),
		newTKCase("3.14159", tk(DOUBLE, "3.14159"), SEMIEOF),
		newTKCase("12345.6789", tk(DOUBLE, "12345.6789"), SEMIEOF),
		newTKCase("12345_.67_89", tk(DOUBLE, "12345.6789"), SEMIEOF),
		newTKCase("2345._", tk(DOUBLE, "2345."), SEMIEOF),
		newTKCase("2_.____", tk(DOUBLE, "2."), SEMIEOF),
		newTKCase("256", tk(INTEGER, "256"), SEMIEOF),
		newTKCase("256_", tk(INTEGER, "256"), SEMIEOF),
		newTKCase("25__________6", tk(INTEGER, "256"), SEMIEOF),
		newTKCase("_256", tk(ID, "_256"), SEMIEOF),
		newTKCase("length - 1", tk(ID, "length"), MINUS, tk(INTEGER, "1"), SEMIEOF),
		newTKCase(
			"3.14159 * 360 + 180 / 50 % 45", 
			tk(DOUBLE, "3.14159"), STAR, tk(INTEGER, "360"), PLUS, tk(INTEGER, "180"), 
			DIV, tk(INTEGER, "50"), MOD, tk(INTEGER, "45"),SEMIEOF
		),
		newTKCase("a > b \n b <===", tk(ID, "a"), GT, tk(ID, "b"), SEMILN, tk(ID, "b"), LTEQ, EQUAL),
		newTKCase("a < b \n b >===", tk(ID, "a"), LT, tk(ID, "b"), SEMILN, tk(ID, "b"), GTEQ, EQUAL),
		newTKCase(
			"AAAA && _b and c or d || e", 
			tk(ID, "AAAA"), tk(LOGICAL_AND, "&&"), tk(ID, "_b"), LOGICAL_AND, 
			tk(ID, "c"), LOGICAL_OR, tk(ID, "d"), tk(LOGICAL_OR, "||"), tk(ID, "e"), SEMIEOF
		),
		newTKCase("{ null\n }", LBRACE, NULL, SEMILN, RBRACE),
		newTKCase("true == false != false", TRUE, EQUAL, FALSE, NOT_EQUAL, FALSE, SEMIEOF),
		newTKCase(
			"(_123 + 4) == (127)", 
			LPAREN, tk(ID, "_123"), PLUS, tk(INTEGER, "4"), RPAREN, EQUAL, LPAREN, 
			tk(INTEGER, "127"), RPAREN, SEMIEOF
		),
		newTKCase("[a, b, c, d]", 
			LBRACKET, tk(ID, "a"), COMMA, tk(ID, "b"), COMMA, 
			tk(ID, "c"), COMMA, tk(ID, "d"), RBRACKET, SEMIEOF
		),
		newTKCase("if () \n{}", IF, LPAREN, RPAREN, SEMILN, LBRACE, RBRACE),
		newTKCase("123456789 \n\n\n\n /** abc */", tk(INTEGER, "123456789"), SEMILN),
		newTKCase("\n\n\n123456789 \n abcd", tk(INTEGER, "123456789"), SEMILN, tk(ID, "abcd"), SEMIEOF),
		newTKCase("() /** \n\n\n */ b", LPAREN, RPAREN, tk(ID, "b"), SEMIEOF),
		newTKCase("() ; \n /** \n\n\n */ b", LPAREN, RPAREN, SEMI, tk(ID, "b"), SEMIEOF),
		newTKCase("\t\n\r() \n /** \t\n\r \n\n\n */ b", LPAREN, RPAREN, SEMILN, tk(ID, "b"), SEMIEOF),
		newTKCase("\t\n\r() ; ", LPAREN, RPAREN, SEMI),
		newTKCase("// what \n what + \n - \n * \n /", tk(ID, "what"), PLUS, MINUS, STAR, DIV),
		newTKCase("var testA = _testA", VAR, tk(ID, "testA"), ASSIGN, tk(ID, "_testA"), SEMIEOF),
		newTKCase("+= -= /= %= *= =", PLUS_ASSIGN, MINUS_ASSIGN, DIV_ASSIGN, MOD_ASSIGN, STAR_ASSIGN, ASSIGN),
		newTKCase("/** sndb */ _println print abc", tk(ID, "_println"), tk(ID, "print"), tk(ID, "abc"), SEMIEOF),
		newTKCase(
			"abc\"Godness\\n\\tI'm bad.\\\"Hello World\\\"\"abc",
			tk(ID, "abc"), tk(STRING, "Godness\n\tI'm bad.\"Hello World\""), tk(ID, "abc"), SEMIEOF
		),
		newTKCase("\"keep keep\''\"", tk(STRING, "keep keep''"), SEMIEOF),
		newTKCase("\"abcdefg\\uFffFF\"", tk(STRING, "abcdefg\uFFFFF"), SEMIEOF),
		newTKCase("\"abcdefg\\u0123F\"", tk(STRING, "abcdefg\u0123F"), SEMIEOF),
		newTKCase("\"abcdefg\\uABCDF\"", tk(STRING, "abcdefg\uABCDF"), SEMIEOF),
		newTKCase("\"abcdefg\\uabCdF\"", tk(STRING, "abcdefg\uABCDF"), SEMIEOF),
		newTKCase("\"abcdefg\\uEeeE\"", tk(STRING, "abcdefg\uEEEE"), SEMIEOF),
		newTKCase("\"abcdefg\\0778\"", tk(STRING, "abcdefg\u003f8"), SEMIEOF),
		newTKCase("\"abcdefg\\077\"", tk(STRING, "abcdefg\u003f"), SEMIEOF),
		newTKCase("\"abcdefg\\200\"", tk(STRING, "abcdefg\u0080"), SEMIEOF),
		newTKCase("\"abcdefg\\377\\u00FF\"", tk(STRING, "abcdefg\u00FF\u00FF"), SEMIEOF),
		newTKCase("\"abcdefg\\000\"", tk(STRING, "abcdefg\u0000"), SEMIEOF),
		newTKCase("\"\"", tk(STRING, ""), SEMIEOF),
		newTKCase(
			"var lambdaExpr = lambda a, b -> a * b", 
			VAR, tk(IDENTIFIER, "lambdaExpr"), ASSIGN, LAMBDA, tk(IDENTIFIER, "a"), COMMA, tk(IDENTIFIER, "b"),
			ARROW, tk(IDENTIFIER, "a"), STAR ,tk(IDENTIFIER, "b"), SEMIEOF
		),
		newTKCase("/** what \n what \n what **********/"),
		newTKCase("// what what what"),
		newTKCase(""),
	} ;
	
	private static Token tk(TokenKind tk) {
		return tk(tk, null) ;
	}
	
	private static Token tk(TokenKind tk, String literal) {
		return new Token(null, literal, tk) ;
	}

	private static TKindAndLiteralCase newTKCase(String input, Object... toks) {
		if (toks == null) toks = new Object[0] ;
		Token[] tks = new Token[toks.length] ;
		for (int i = 0; i < toks.length; i ++) {
			if (toks[i] instanceof Token) {
				tks[i] = (Token) toks[i] ;
			} else {
				tks[i] = tk((TokenKind) toks[i]) ;
			}
		}
		return new TKindAndLiteralCase(input, tks) ;
	}

	private static class TKindAndLiteralCase implements CandyTestCase{
		String input ;
		Token[] expectedToks ;
		ArrayList<Token> actualToks ;

		public TKindAndLiteralCase(String input, Token[] expectedToks) {
			this.input = input;
			this.expectedToks = expectedToks;
			this.actualToks = new ArrayList<>() ;
		}

		@Override
		public void assertCase() {
			Scanner scanner = ScannerFactory.newScanner("test", input) ;

			LoggerMsgChecker.shouldNotAppearErrors(true, true);

			while (scanner.hasNextToken()) {
				Token tk = scanner.peek() ;
				actualToks.add(tk(tk.getKind(), tk.getLiteral())) ;
				scanner.nextToken() ;
			}
			assertEquals(msg(), expectedToks.length, actualToks.size()) ;
			for (int i = 0; i < expectedToks.length; i ++) {
				Token expected = expectedToks[i] ;
				Token actual = actualToks.get(i) ;
				assertEquals(msg(), expected.getKind(), actual.getKind()) ;
				assertEquals(msg(), expected.getLiteral(), actual.getLiteral()) ;
			}
		}
		
		private String msg() {
			return "Input: <" + input +  
			       ">\nExpected: " + Arrays.toString(expectedToks) +
				   "\nWas: " + actualToks.toString() + "\n" ;
		}
	}
	
	static final CandyTestCase[] PE_TEST_CASES = {
		newPECase("abc&def|", true, pos(1, 4), pos(1, 8)),
		
		newPECase("\n\n abcdefg", false, pos(3, 2), pos(3, 9)),
		newPECase("\n\t\t_abc 123 \n", false, pos(2, 3), pos(2, 8), pos(2,12)),
		newPECase("\n//\t\t_abc 123\n\t/*abcd*/abcd/**/", false, pos(3, 10), pos(3, 18)),
		newPECase("\n\"Oh my god! Are you ok?\"\n\n bbbbb", false, pos(2, 1), pos(2, 25), pos(4, 2), pos(4, 7)),
		newPECase("/** abc.\n\n\t\t*/≈≈___╳", true, pos(3, 5), pos(3, 6), pos(3, 10)),
		newPECase("\n\n/** good good", true, pos(3,14)),
		newPECase("/**abcd", true, pos(1, 8)),
		newPECase("\"music\nIn the end\"", true, pos(1, 7), pos(2, 12)),
		newPECase("\"\\uFFFR\"", true, pos(1, 7)),
		newPECase("\"\\u\"", true, pos(1, 4), pos(1, 5)),
		newPECase("\"\\888\"", true, pos(1, 3)),
		newPECase("\"\\788\"", true, pos(1, 4)),
		newPECase("\"\\777\"", true, pos(1, 5)),
	} ;
	
	public static SimulationPositions.Location pos(int line, int col) {
		return SimulationPositions.loc(line, col) ;
	}
	
	public static PositionAndErrorCase newPECase(String input, boolean error, SimulationPositions.Location... locs) {
		return new PositionAndErrorCase(input, error, locs) ;
	}
	
	private static class PositionAndErrorCase implements CandyTestCase {

		SimulationPositions expected ;
		boolean hasError ;
		ArrayList<Position> actualPos = new ArrayList<>() ;

		public PositionAndErrorCase(String input, boolean hasError, SimulationPositions.Location[] locs) {
			this.expected = new SimulationPositions(input, locs) ;
			this.hasError = hasError;
		}
		
		@Override
		public void assertCase() {
			Scanner scanner = ScannerFactory.newScanner("test.cd", expected.input) ;
			while (scanner.hasNextToken()) {
				Token tk = scanner.peek() ;
				if (!hasError) {
					actualPos.add(tk.getPos()) ;
				}
				scanner.nextToken() ;
			}
			if (hasError) {
				assertTrue(logger.hadErrors()) ;
				for (Logger.LogMessage msg : logger.getErrorMessages()) {
					actualPos.add(msg.getPos()) ;
				}
				try {
					logger.printErrors(System.err) ;
				} catch (IOException e) {
					fail(e.getMessage()) ;
				}
				logger.clearAllMessages() ;
			}
			assertArrayEquals(expected.positions, actualPos.toArray()) ;
		}
	}
	
	
	
}
