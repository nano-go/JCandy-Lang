package com.nano.candy.parser;

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
		"var a = 1..2",
		"var a = 1*2..2*5",
		"var a = 1*2..2>5",
		"var arr = [1, 5 + 6, lambda a,b -> 9, c] + 123 * 5",
		"var arr = [1, 5 + 6, a = 15, false]",
		"var arr = [1, 5 + 6, a = lambda a -> b]",
		"var arr = [[c, cpp, cs, lambda lisp -> lisp], id, name, java]",
		"var arr = [ \n[1, 2, 3, 4],\n [5,6,7,8],\n[9, 10, 11, 12]\n]",
		"var arr = [ \n[1, 2, 3, 4],\n [5,6,7,8],\n[9, 10, 11, 12],\n]",
		"var map = {}",
		"var map = {\"a\": 5,\n \"b\" + c: 10,}",
		"var map = {\"a\": 5,\n c: 10 + 5}",
		"var map = {{\"a\": b, c: d}: lambda e -> good, key: value, key: {key: value}}",
		"var c = true ? 1 + 2 : false",
		"a or b or c ? (a or b or c ? a : b or b) : false",
		"[]",
		"if (false) var a = 0\nelse if (true) var b = 155 ; else if (a + b * c) true",
		"if (false) var a = 0\nelse var b = 155",
		"if (false) \n a += true",
		"if (false) { a += true; }",
		"if (false) \n { a += true; } else if (a > b) \n { a += false; }",
		"if (false) { a += true; } else if (a > b) \n a += false; ",
		"while (true) var a = b",
		"while (true) break",
		"while (true) \n break",
		"while (true) { break; }",
		"while (false) \n {var a = 66\nif (a) \n { break ; }}",
		"try {\n\n} \n",
		"try {}",
		"try {} intercept e1 {} intercept IOException as e2 {}",
		"try {} intercept as e1 {} intercept IOException as e2 {}",
		"try {} intercept e1 + 5, 6 as n {} intercept IOException as e2 {}",
		"try {} intercept IOException, Exception, Erroe as e1 {}",
		"try {\n\n} intercept IOException as e {} intercept e { if(true) {}} else {}",
		"try {\n\n} \n intercept IOException as e \n {}",
		"try {\n\n} \n intercept IOException as e \n {} else {}",
		"raise Exception(4,3)",
		"raise AnyException() + 5",
		"for (i in range(0, 7)) break",
		"for (i in range(0, 7)) { break; }",
		"for (i in b) { b; }",
		"for (i in b) \n b;",
		"for (i in b) \n { b; }",
		"for (i in a = lambda a,b -> a*b) {\nprintln(i);}",
		"fun test(a, b, c) { return 0 }",
		"fun test(a, b, c) { return; }",
		"fun test(a, b, c) { a * b * c }",
		"fun test(a, b, c) \n { a * b * c ; }",
		"fun test(a, *b) {}",
		"fun test(*a) {}",
		"test(a, *b)",
		"test(*a, b)",
		"test(lambda a, b -> if (a > b) b else c)",
		"test(lambda a, *b -> if (a > b) b else return c)",	
		"test(a, b -> return b)",
		"test(a, () -> return a + b)",
		"test(a, (b, c) -> return a + b)",
		"test(a, || -> return)",
		"test(a, | | -> return)",
		"test(a, -> return)",
		"test(a, -> return {})",
		"test(a, |a, *b| -> return a + b)",
		"test(a, |b, c| -> return c)",
		"var lambdaExpr = lambda a, b -> a * b",
		"var lambdaExpr = lambda a, b -> (a * b)",
		"var lambdaExpr = lambda a, b -> { a * b\n }",
		"var lambdaExpr = lambda a, b -> {}",
		"var lambdaExpr = lambda a, b -> return b",
		"var lambdaExpr = () -> return;",
		"var lambdaExpr = (a) -> return a",
		"var lambdaExpr = (a, b) -> return a + b",
		"var lambdaExpr = -> return ->{}",
		"var lambdaExpr = | | -> return ||->{}",
		"var lambdaExpr = |a, b| -> return b",
		"var lambdaExpr = |a, *b| -> return b",
		"var lambdaExpr = |a, *b, c| -> return b",
		"var lambdaExpr = |a| -> { return b; }",
		"var lambdaExpr = a -> return b",
		"var a = lambda a -> return lambda a -> a",
		"a.b.c = 1566 * 55\n manager.getUser(5).getId = lambda -> return user.id",
		"stream()\n\t.filter(a).map(b).\nlimit(5)\n.skip(15)\n",
		"_call(a, lambda a -> a)",
		"_call(lambda a -> a)",
		"class Point {\n x() { \n return x; } y() { \n return y; } }",
		"class P {\n P() {this.x = x; this.y = y;} A() { super.test().b; } }",
		"class A { fun test() {super.a(); super.b();}}",
		"class A \n {}",
		"class A : a.b {}",
		"class A : a[c] {}",
		"class A \n { fun a(a, b) \n {} fun a() \n {} fun a(a, b) {} }",
		"class A {fun init(value) {\n" + 
			"@if = a\n" +
			"@value = value\n" +
		"}}",
		
		"class A {\n" +
		"    static {" +
		"	     if (a) foo()\n" +
		"	     @a = 5\n" +
		"	     this.b = 5\n" +
		"        fun @foo() {@bar = 15;}" + 
		"    }" +
		"}",
		
		"class A {\n" + 
		"    static var a = 5\n" +
		"    static var b = @a\n" + 
		"    static fun test() { " +
		"        println(@a)\n" +
		"    }\n" +
		"    static class B {}\n" +
		"}",
		
		"class A {fun a(value) {\n" + 
			"return @a\n" +
		"}}",
		"import \"stream.cd\" as stream;",
		"import a + b as stream",
		"import (\n\"stream\" as stream\na + b as stream\n)",
		"import ()",
		"-> {}",
		"|| -> {}",
		"|a, *b| -> {}",
		"(a, b,) -> {}",
		"(a) -> {}",
		"(a, b + 15 / 3, c(a(), b()()))",
		"(a, b,)",
		"(a,)",
		"()",
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
		newPECase("var a =\n\tlambda => {\n\tGoGo(true)\n}", loc(2, 9)),
		newPECase("or -> {}", loc(1, 1)),
		newPECase("var ", loc(1, 5), loc(1, 5)),
		newPECase("a. ", loc(1, 4)),
		newPECase("fun ", loc(1, 5), loc(1, 5), loc(1, 5)),
		newPECase("fun a(a, *) {}", loc(1, 11)),
		newPECase("class ", loc(1, 7), loc(1, 7), loc(1, 7)),
		newPECase("class a :", loc(1, 10), loc(1, 10), loc(1, 10)),
		newPECase("class A { fun test() {super.a(); super.b();}\nfalse}\n", loc(1, 44)),
		
		newPECase(
			"maxParams(a0,a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,a11,a12,a13,a14,a15,a16,a17,a18,a19,a20,a21,a22,a23,a24,a25,a26,a27,a28,a29,a30,a31,a32,a33,a34,a35,a36,a37,a38,a39,a40,a41,a42,a43,a44,a45,a46,a47,a48,a49,a50,a51,a52,a53,a54,a55,a56,a57,a58,a59,a60,a61,a62,a63,a64,a65,a66,a67,a68,a69,a70,a71,a72,a73,a74,a75,a76,a77,a78,a79,a80,a81,a82,a83,a84,a85,a86,a87,a88,a89,a90,a91,a92,a93,a94,a95,a96,a97,a98,a99,a100,a101,a102,a103,a104,a105,a106,a107,a108,a109,a110,a111,a112,a113,a114,a115,a116,a117,a118,a119,a120,a121,a122,a123,a124,a125,a126,a127,a128,a129,a130,a131,a132,a133,a134,a135,a136,a137,a138,a139,a140,a141,a142,a143,a144,a145,a146,a147,a148,a149,a150,a151,a152,a153,a154,a155,a156,a157,a158,a159,a160,a161,a162,a163,a164,a165,a166,a167,a168,a169,a170,a171,a172,a173,a174,a175,a176,a177,a178,a179,a180,a181,a182,a183,a184,a185,a186,a187,a188,a189,a190,a191,a192,a193,a194,a195,a196,a197,a198,a199,a200,a201,a202,a203,a204,a205,a206,a207,a208,a209,a210,a211,a212,a213,a214,a215,a216,a217,a218,a219,a220,a221,a222,a223,a224,a225,a226,a227,a228,a229,a230,a231,a232,a233,a234,a235,a236,a237,a238,a239,a240,a241,a242,a243,a244,a245,a246,a247,a248,a249,a250,a251,a252,a253,a254,a255)", 
			loc(1, 10)
		),
		newPECase("break;", loc(1, 1)),
		newPECase("continue;", loc(1, 1)),
		newPECase("return;", loc(1, 1)),
		newPECase("this;", loc(1, 1)),
		newPECase("super.init();", loc(1, 1)),
		newPECase("while (true) { break;\na -;\n}\nbreak;", loc(2, 4), loc(4, 1)),
		newPECase("while (true) { continue;\na -;\n}\ncontinue;", loc(2, 4), loc(4, 1)),
		newPECase("for (i in arr) { break;\na -;\n}\nbreak;", loc(2, 4), loc(4, 1)),
		newPECase("for (i in arr) { continue;\na -;\n}\ncontinue;", loc(2, 4), loc(4, 1)),
		newPECase("fun test(a, b, a) {}", loc(1, 1)),
		newPECase(
			"fun maxParams(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19,p20,p21,p22,p23,p24,p25,p26,p27,p28,p29,p30,p31,p32,p33,p34,p35,p36,p37,p38,p39,p40,p41,p42,p43,p44,p45,p46,p47,p48,p49,p50,p51,p52,p53,p54,p55,p56,p57,p58,p59,p60,p61,p62,p63,p64,p65,p66,p67,p68,p69,p70,p71,p72,p73,p74,p75,p76,p77,p78,p79,p80,p81,p82,p83,p84,p85,p86,p87,p88,p89,p90,p91,p92,p93,p94,p95,p96,p97,p98,p99,p100,p101,p102,p103,p104,p105,p106,p107,p108,p109,p110,p111,p112,p113,p114,p115,p116,p117,p118,p119,p120,p121,p122,p123,p124,p125,p126,p127,p128,p129,p130,p131,p132,p133,p134,p135,p136,p137,p138,p139,p140,p141,p142,p143,p144,p145,p146,p147,p148,p149,p150,p151,p152,p153,p154,p155,p156,p157,p158,p159,p160,p161,p162,p163,p164,p165,p166,p167,p168,p169,p170,p171,p172,p173,p174,p175,p176,p177,p178,p179,p180,p181,p182,p183,p184,p185,p186,p187,p188,p189,p190,p191,p192,p193,p194,p195,p196,p197,p198,p199,p200,p201,p202,p203,p204,p205,p206,p207,p208,p209,p210,p211,p212,p213,p214,p215,p216,p217,p218,p219,p220,p221,p222,p223,p224,p225,p226,p227,p228,p229,p230,p231,p232,p233,p234,p235,p236,p237,p238,p239,p240,p241,p242,p243,p244,p245,p246,p247,p248,p249,p250,p251,p252,p253,p254,p255) {}", 
			loc(1, 1)
		),
		newPECase("fun a() {\nsuper.init();\n}", loc(2, 1)),
		newPECase("fun a() {\nreturn a \na -;\n}\nreturn", loc(3, 4), loc(5, 1)),
		newPECase("var a = lambda -> {\nreturn a \na -;\n}\nreturn", loc(3, 4), loc(5, 1)),
		newPECase("class A { fun a(){\n return a \na -;\n}}\nreturn", loc(3, 4), loc(5, 1)),
		newPECase("class A {\nfun init() {\nreturn a; }}", loc(3, 1)),
	};
	 
	public static ParserErrorCase newPECase(String input, SimulationPositions.Location... expectedLocations) {
		return new ParserErrorCase(input, expectedLocations);
	}
	
	private static class ParserErrorCase {
		SimulationPositions expected ;
		ArrayList<Position> actualPos ;

		public ParserErrorCase(String input, SimulationPositions.Location... expected) {
			this.expected = new SimulationPositions(input, expected);
			this.actualPos = new ArrayList<>() ;
		}
		public void assertErrors() {
			Parser parser = new CandyParser(ScannerFactory.newScanner("test.cd", expected.input));
			parser.parse();
			for (Logger.LogMessage msg : logger.getErrorMessages()) {
				actualPos.add(msg.getPos()) ;
			}
			LoggerMsgChecker.expectedErrors(true, true, expected.input);
			assertArrayEquals(expected.input, expected.positions, actualPos.toArray());
		}
	}
	
	@Test public void testErrors() {
		for (ParserErrorCase pe : PARSER_ERROR_CASES) {
			pe.assertErrors();
		}
	}

}
