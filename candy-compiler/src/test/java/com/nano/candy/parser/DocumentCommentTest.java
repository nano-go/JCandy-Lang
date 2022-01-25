package com.nano.candy.parser;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.utils.ArrayUtils;
import com.nano.candy.utils.CandySourceFile;
import com.nano.candy.utils.Context;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Phase;
import java.io.IOException;
import org.junit.Test;

import static org.junit.Assert.*;

public class DocumentCommentTest {
	
	public static final String[] SYNTAX_TEST = {
		"",
		"/* Hello */ /* Hi */;",
		"/* H */;/* H */",
		"/* Hello */ /* Hi */ class /*H*/ A /*H*/ \n /*H*/\n{}/**/",
		";/* Var a */ var a; /* Var b */ var b /*H*/ \n",
		"var a /*H*/ /*H*/ = a /*H*/ -> /*H*/ /*H*/ a + /*H*/ b/*H*/\nvar b",
		"/* T */ fun returnABoolean /* T */ () /*T*/\n {\n return /*T*/ true;}",
		"fun /*A*//*A*/ ret() {}",
		"/*A*/\"abc${/**/d/**/+\\\"/**/E/**/\\\"}hhh${/*A*/}\"",
		builtMultiLine(
			"class A : B {",
			"\t/*P*/pri /**//**/ a /**//**/, b",
			"\t/*R*/reader /**//**/ a /**//**/, b",
			"\t/*W*/writer /**//**/ a /**//**/, b",
			"\t/*P*/pub /**//**/ a /**//**/, b",
			"\t/*H*/fun /**//**/ init /**/ (/**/) {}",
			"\t/*b*/fun /**//**/ A /**/(/**/) {}",
			"\t/*B*/static /**/{/**/}",
			"\t/*S*/static var /*a*/ a",
			"\t/*D*/static fun a(){}",
			"}"
		),
	};
	
	private static String builtMultiLine(String... lines) {
		return "\n".join("", lines);
	}
	
	@Test public void syntaxTest() throws IOException {
		Context ctx = new Context();
		Phase<CandySourceFile, ASTreeNode> parser =
			new ParserBuilder(ctx).setKeepComment(true).newPhase();
		String[] TEST_ARRAY = ArrayUtils.mergeArray(
			SYNTAX_TEST, 
			ParserTest.UNEXPECTED_ERROR_CASES
		);
		for (String s : TEST_ARRAY) {
			parser.apply(ctx, new CandySourceFile("syntax_test.cd", s));
			ctx.get(Logger.class).printAllMessage(false);
			assertFalse(ctx.get(Logger.class).hadErrors());
		}
	}
	
}
