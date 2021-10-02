package com.nano.candy.parser;

import com.nano.candy.utils.Context;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Position;
import java.io.IOException;
import java.util.ArrayList;

import static com.nano.candy.parser.TokenKind.*;
import static org.junit.Assert.*;

public class ScannerTestCase {
	
	private static final Token SEMIEOF= tk(TokenKind.SEMI, "EOF") ;
	private static final Token SEMILN= tk(TokenKind.SEMI, Token.LN_SEMI_LITERAL) ;
	
	public static Logger getLogger() {
		return Context.getThreadLocalContext().get(Logger.class);
	}
	
	public static ArrayList<Token> toTokenList(Scanner scanner) {
		ArrayList<Token> toks = new ArrayList<>();
		while (scanner.peek().getKind() != TokenKind.EOF) {
			toks.add(scanner.peek());
			scanner.nextToken();
		}
		return toks;
	}
	
	/**
	 * Token stream test case.
	 *
	 * Format: Input, Token(TokenKind, [ Literal ])...
	 */ 
	protected static final CandyTestCase[] TOK_STREAM_TEST_CASES = {
		
		/*====== Test whether comments will affect insertSemi. ======*/
		
		newTKCase(
			"Test__112344__Abc // comment...\n", 
			id("Test__112344__Abc"), SEMILN
		),
		newTKCase(
			"_abC // comment...", 
			id("_abC"), SEMIEOF
		),
		newTKCase(
			" _123bba /** comment...\nabcd*/\n", 
			id("_123bba"), SEMILN
		),
		newTKCase(
			"_abC /** comment...*/", 
			id("_abC"), SEMIEOF
		),
		newTKCase(
			"\t\n\r() \n /** \t\n\r \n\n\n */ b", 
			LPAREN, RPAREN, SEMILN, id("b"), SEMIEOF
		),
		
		
		/*===== Name Test Case ======*/
		
		newTKCase(
			"\n\t\t_abc \n", 
			id("_abc"), 
			SEMILN
		),
		newTKCase(
			"_", id("_"), SEMIEOF
		),
		newTKCase(
			"length -", 
			id("length"), MINUS
		),
		newTKCase(
			"\nabc12_34\n",
			id("abc12_34"), SEMILN
		),
		newTKCase(
			"@abcd @if @while",
			atid("abcd"), atid("if"), atid("while"), SEMIEOF
		),
		newTKCase(
			"\n@_abc@_123\n",
			atid("_abc"), atid("_123"), SEMILN
		),
		
		
		/*===== String Literal Test Case =====*/
		
		newTKCase(
			"\"keep keep\''\"",
			str("keep keep''"), SEMIEOF
		),
		newTKCase(
			"\"abcdefg\\uFffFF\"", 
			str("abcdefg\uFFFFF"), SEMIEOF
		),
		newTKCase(
			"\"abcdefg\\u0123F\"", 
			str("abcdefg\u0123F"), SEMIEOF
		),
		newTKCase(
			"\"abcdefg\\uABCDF\"", 
			str("abcdefg\uABCDF"), SEMIEOF
		),
		newTKCase(
			"\"abcdefg\\uabCdF\"",
			str("abcdefg\uABCDF"), SEMIEOF
		),
		newTKCase(
			"\"abcdefg\\uEeeE\"", 
			str("abcdefg\uEEEE"), SEMIEOF
		),
		newTKCase(
			"\"abcdefg\\u0000\"",
			str("abcdefg\u0000"), SEMIEOF
		),
		newTKCase(
			"\"abcdefg\\0778\"", 
			str("abcdefg\u003f8"), SEMIEOF
		),
		newTKCase(
			"\"abcdefg\\077\"", 
			str("abcdefg\u003f"), SEMIEOF
		),
		newTKCase(
			"\"abcdefg\\200\"", 
			str("abcdefg\u0080"), SEMIEOF
		),
		newTKCase(
			"\"abcdefg\\377\\u00FF\"", 
			str("abcdefg\u00FF\u00FF"), SEMIEOF
		),
		newTKCase(
			"\"abcdefg\\000\"", 
			str("abcdefg\u0000"), SEMIEOF
		),
		newTKCase("\"\"", str(""), SEMIEOF),
		
		/* ===== Interpolated String ===== */
		
		newTKCase("\"a${a+b}c\"", 
			istr("a"), id("a"), PLUS, id("b"), str("c"), SEMIEOF
		),
		newTKCase("\"${a+b}\"", 
			istr(""), id("a"), PLUS, id("b"), str(""), SEMIEOF
		),
		newTKCase("\"aa${get(\\\"${bc} d\\\")} e\"", 
			istr("aa"), id("get"), LPAREN, istr(""), id("bc"),
			str(" d"), RPAREN, str(" e"), SEMIEOF
		),
		newTKCase("\"a${b\\\"c${\\\"d\\\"}\\\"}\"", 
			istr("a"), id("b"), istr("c"), str("d"), 
			str(""), str(""), SEMIEOF
		),
		newTKCase("\"a${\\\"b${\\\"c${\\\"d${\\\"\\\"}\\\"}\\\"}\\\"}\"",
			istr("a"), istr("b"), istr("c"), istr("d"),
			str(""), str(""), str(""), str(""), str(""),
			SEMIEOF
		),
		newTKCase("\"Lmabda: ${foreach(lambda -> {return a;\\})}\"",
			istr("Lmabda: "), id("foreach"), LPAREN, LAMBDA,
			ARROW, LBRACE, RETURN, id("a"), SEMI, RBRACE, RPAREN,
			str(""), SEMIEOF
		),
		newTKCase("\"The a value is ${a}, The a.b value is ${a.b}.\"",
			istr("The a value is "), id("a"), istr(", The a.b value is "),
			id("a"), DOT, id("b"), str("."), SEMIEOF
		),
		newTKCase("\"${\\\"\\\" + \\\"${a + \\\"\\\"}\\\"} ${a}\"",
			istr(""), str(""), PLUS, istr(""), id("a"), PLUS, str(""), str(""),
			istr(" "), id("a"), str(""), SEMIEOF
		),
		
		/*===== InsertSemi Test Case =====*/
		
		newTKCase(
			"() /** \n\n\n */ b", 
			LPAREN, RPAREN, 
			id("b"), SEMIEOF
		),
		newTKCase(
			"() ; \n /** \n\n\n */ b", 
			LPAREN, RPAREN, SEMI, 
			id("b"), SEMIEOF
		),
		newTKCase(
			"\t\n\r()",
			LPAREN, RPAREN, SEMIEOF
		),
		newTKCase(
			"\n[\n]",
			LBRACKET, RBRACKET, SEMIEOF
		),
		newTKCase(
			"\n1234",
			integer("1234"), SEMIEOF
		),
		newTKCase(
			"\n1234\n",
			integer("1234"), SEMILN
		),
		newTKCase(
			"\n12.34",
			doubl("12.34"), SEMIEOF
		),
		newTKCase(
			"\n12.34\n",
			doubl("12.34"), SEMILN
		),
		newTKCase("123..", integer("123"), DOT_DOT),
		
		/*===== Operator Test Case =====*/
		
		newTKCase("a ? b : c", 
			id("a"), QUESITION, id("b"), COLON, id("c"), SEMIEOF),
		
		newTKCase(
			"+ - * / % is == != > >= < <= >> << and or && || ..", 
			PLUS, MINUS, STAR, DIV, MOD, 
			IS, EQUAL, NOT_EQUAL,
			GT, GTEQ, LT, LTEQ,
			RIGHT_SHIFT, LEFT_SHIFT,
			tk(LOGICAL_AND, "and"), tk(LOGICAL_OR, "or"),
			tk(LOGICAL_AND, "&&"), tk(LOGICAL_OR, "||"),
			DOT_DOT
		),
		newTKCase(
			"= += -= *= /= %= >>= <<=",
			ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, 
			STAR_ASSIGN, DIV_ASSIGN, MOD_ASSIGN, 
			RIGHT_SHIFT_ASSIGN, LEFT_SHIFT_ASSIGN
		),
		newTKCase(
			">===", GTEQ, EQUAL
		),
		newTKCase(
			"<===", LTEQ, EQUAL
		),
		newTKCase(
			"3.14159 * 0X360+0170 / 50 % 45", 
			doubl("3.14159"), STAR, 
			integer("0X360"), PLUS, 
			integer("0170"), DIV, 
			integer("50"), MOD, 
			integer("45"),SEMIEOF
		),
		
		newTKCase("123.abc", integer("123"), DOT, id("abc"), SEMIEOF),
		newTKCase("123.", integer("123"), DOT),
		newTKCase("123..", integer("123"), DOT_DOT),
		
		
		/*===== KeyWord Test Case ===== */
		newTKCase(
			"if else while for in var fun class lambda return " + 
			"this super break continue assert import as raise intercept try",
			IF, ELSE, WHILE, FOR, IN, VAR, FUN, CLASS, LAMBDA,
			RETURN, THIS, SUPER, BREAK, CONTINUE, ASSERT, IMPORT, AS,
			RAISE, INTERCEPT, TRY
		),
		newTKCase(
			"true\n false \n null \n",
			TRUE, SEMILN,
			FALSE, SEMILN,
			NULL, SEMILN
		),
		
		/*===== Other =====*/
		newTKCase(
			"0XFFFFFGB", 
			integer("0XFFFFF"), id("GB"), SEMIEOF
		),
		newTKCase(
			"{ null\n: }", 
			LBRACE, NULL, SEMILN, COLON, RBRACE
		),
		newTKCase(
			"| a |",
			BIT_OR, id("a"), BIT_OR
		),
		newTKCase("a.b", id("a"), DOT, id("b"), SEMIEOF),
		newTKCase("[a, b, c, d]", 
			LBRACKET, id("a"), COMMA, id("b"), COMMA, 
			id("c"), COMMA, id("d"), RBRACKET, SEMIEOF
		)
	};
	
	private static Token integer(String intLiteral) {
		return tk(INTEGER, intLiteral);
	}
	
	private static Token doubl(String doubleLiteral) {
		return tk(DOUBLE, doubleLiteral);
	}
	
	private static Token id(String name) {
		return tk(IDENTIFIER, name);
	}
	
	private static Token atid(String name) {
		return tk(AT_IDENTIFIER, name);
	}
	
	private static Token str(String str) {
		return tk(STRING, str);
	}
	
	private static Token istr(String str) {
		return tk(INTERPOLATION, str);
	}
	
	private static Token tk(TokenKind tk) {
		return tk(tk, tk.literal) ;
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
	
	/**
	 * Format: Input, TokenKind, Value
	 */
	protected static final CandyTestCase[] NUMBER_LITERAL_CASES = {
		newNLCase("12345", INTEGER, 12345),
		newNLCase("0xFFFF", INTEGER, 0xFFFF),
		newNLCase("0XFFFF", INTEGER, 0xFFFF),
		newNLCase("0x00ABFF1200EF", INTEGER, 0x00AB_FF_12_00_EFL),
		newNLCase("01234567", INTEGER, 01234567),
		newNLCase("0o1234567", INTEGER, 01234567),
		newNLCase("0O1234567", INTEGER, 01234567),
		newNLCase("0b000111", INTEGER, 0b000111),
		newNLCase("0b000101", INTEGER, 0b000101),
		newNLCase("0B000000", INTEGER, 0b000000),
		
		newNLCase("123_456", INTEGER, 123456),
		newNLCase("1__2__3__", INTEGER, 123),
		newNLCase("0xCA_FE_BA_BE", INTEGER, 0xCAFEBABEL),
		newNLCase("0b0000_1001", INTEGER, 0b00001001),
		newNLCase("3_._14159_265358_9793", DOUBLE, 3.141592653589793D),
		newNLCase("0._35", DOUBLE, 0.35),
		newNLCase("0.35", DOUBLE, 0.35),
		newNLCase("0._123", DOUBLE, 0.123),
	
		newNLCase("0", INTEGER, 0),
		newNLCase("0b", INTEGER, 0),
		newNLCase("0o", INTEGER, 0),
		newNLCase("0x", INTEGER, 0),
		newNLCase("0.0", DOUBLE, 0),
	};

	private static NumberLiteralCase newNLCase(String input, TokenKind kind, double value) {
		return new NumberLiteralCase(input, kind, value);
	}
	
	/**
	 * Format: Input, hasError, Positions...
	 * These 'Positions' are where the error is occured if hasError is true.
	 */
	static final CandyTestCase[] POSITION_AND_ERROR_TEST_CASES = {
		
		newPECase("\n\n abcdefg", false, pos(3, 2), pos(3, 9)),
		newPECase("\n\t\t_abc 123 \n", false, pos(2, 3), pos(2, 8), pos(2,12)),
		newPECase("\n//\t\t_abc 123\n\t/*abcd*/abcd/**/", false, pos(3, 10), pos(3, 18)),
		newPECase("\n\"Oh my god! Are you ok?\"\n\n bbbbb", false, pos(2, 1), pos(2, 25), pos(4, 2), pos(4, 7)),
		
		/* @ IDENTIFIER */
		newPECase("@123", true, pos(1, 2)),
		newPECase("@", true, pos(1, 2)),
		
		/* Comment */
		newPECase("/** abc.\n\n\t\t*/≈≈___╳", true, pos(3, 5), pos(3, 6), pos(3, 10)),
		newPECase("\n\n/** good good", true, pos(3,14)),
		newPECase("/**abcd", true, pos(1, 8)),
		
		/* String Literal */	
		newPECase("\"music\nIn the end\"", true, pos(1, 7), pos(2, 12)),
		newPECase("\"\\uFFFR\"", true, pos(1, 7)),
		newPECase("\"\\u\"", true, pos(1, 4), pos(1, 5)),
		newPECase("\"\\888\"", true, pos(1, 3)),
		newPECase("\"\\788\"", true, pos(1, 4)),
		newPECase("\"\\777\"", true, pos(1, 5)),
		
		/* Interpolated String */
		newPECase("\"${}\"", true, pos(1, 4)),
		newPECase("\"a${\"", true, pos(1, 5)),
		newPECase("\"a$\"", true, pos(1, 4)),
		newPECase("\"$\"", true, pos(1, 3)),
		newPECase("\"${-", true, pos(1, 5), pos(1, 5)),
		newPECase("\"${- ", true, pos(1, 6), pos(1, 6)),
		newPECase("\"${a ", true, pos(1, 6), pos(1, 6)),
		newPECase("\"${\\\"}\"", true, pos(1, 7), pos(1, 7)),
		newPECase("\"${\\a}\"", true, pos(1, 5)),
		newPECase("\"${\n", true, pos(1, 4), pos(1, 4)),
		
		/* Number Literal */	
		newPECase("0bFFFF", true, pos(1, 3), pos(1, 4), pos(1, 5), pos(1, 6)),
		newPECase("0b002", true, pos(1, 5)),
		newPECase("089", true, pos(1, 2), pos(1, 3)),
		newPECase("0o89", true, pos(1, 3), pos(1, 4)),
		newPECase("01.6", true, pos(1, 3)),
		newPECase("123456FF", true, pos(1, 7), pos(1, 8)),
		newPECase("0o000.8", true, pos(1, 6), pos(1, 7)),
		newPECase("0xFFFFFFFFFFFFFFFFFFFFF", true, pos(1, 1)),
		newPECase("0B1111111111111111111111111111111111111111111111111111111111111111111111", true, pos(1, 1)),
		
		/* Unknown Character */
		newPECase("αβγδ", true, pos(1, 1), pos(1, 2), pos(1, 3), pos(1, 4)),
	} ;

	public static SimulationPositions.Location pos(int line, int col) {
		return SimulationPositions.loc(line, col) ;
	}

	public static PositionAndErrorCase newPECase(String input, boolean error, SimulationPositions.Location... locs) {
		return new PositionAndErrorCase(input, error, locs) ;
	}
	
	
	
	/**
	 * Token Stream Test. Checks kind and literal.
	 */
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
			Scanner scanner = ScannerFactory.newScanner("token stream", input) ;

			while (scanner.hasNextToken()) {
				Token tk = scanner.peek() ;
				actualToks.add(tk(tk.getKind(), tk.getLiteral())) ;
				scanner.nextToken() ;
			}
			
			LoggerMsgChecker.unexpectedErrors(getLogger(), true, true);
			
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
			       ">\nExpected: " + 
				   tokArr2Str(expectedToks) +
				   "\nWas: " + tokArr2Str(actualToks.toArray(new Token[0]));
		}
		
		private static String tokArr2Str(Token[] toks) {
			StringBuilder builder = new StringBuilder();
			int i = 0;
			for (Token tok : toks) {
				builder.append(i ++).append(": ").append(tok).append("\n");
			}
			return builder.toString();
		}
	}
	
	/**
	 * Number and Value Test.
	 */
	public static class NumberLiteralCase implements CandyTestCase {

		private String literal;
		private TokenKind kind;
		
		// the type of this value maybe long or double,
		// and the double value is used here for the comparation,
		private double value;

		public NumberLiteralCase(String literal, TokenKind kind, double value) {
			this.literal = literal;
			this.kind = kind;
			this.value = value;
		}
		
		@Override
		public void assertCase() {
			Scanner scanner = ScannerFactory.newScanner("number literal", literal);
			Token tok = scanner.peek();
			LoggerMsgChecker.unexpectedErrors(getLogger(), true, true);
			
			assertEquals("Input:" + literal, kind, tok.getKind());
			if (kind == TokenKind.DOUBLE) {
				assertEquals(
					"Input:" + literal, 
					Double.valueOf(value), 
					Double.valueOf(((Token.DoubleNumberToken) tok).getValue())
				);
			} else {
				assertEquals(
					"Input:" + literal, 
					Double.valueOf(value), 
					Double.valueOf(((Token.IntegerNumberToken) tok).getValue())
				);
			}
			
			// a test case has only a number literal.
			assertTrue(
				scanner.nextToken().getKind() == SEMI
			);
		}
	}
	
	/**
	 * Position and Error Test.
	 */
	private static class PositionAndErrorCase implements CandyTestCase {

		SimulationPositions expected;
		boolean hasError;
		ArrayList<Position> actualPos = new ArrayList<>();

		public PositionAndErrorCase(String input, boolean hasError, SimulationPositions.Location[] locs) {
			this.expected = new SimulationPositions(input, locs);
			this.hasError = hasError;
		}
		
		@Override
		public void assertCase() {
			Logger logger = getLogger();
			Scanner scanner = ScannerFactory.newScanner("position and error", expected.input);
			while (scanner.hasNextToken()) {
				Token tk = scanner.peek();
				if (!hasError) {
					actualPos.add(tk.getPos());
				}
				scanner.nextToken();
			}
			
			if (hasError) {
				assertTrue(logger.hadErrors());
				for (Logger.LogMessage msg : logger.getErrorMessages()) {
					actualPos.add(msg.getPos());
				}
				try {
					logger.printErrors(System.err);
				} catch (IOException e) {
					fail(e.getMessage());
				} finally {
					logger.clearAllMessages();
				}
			}
			assertArrayEquals(expected.positions, actualPos.toArray());
		}
	}
	
	
	
}
