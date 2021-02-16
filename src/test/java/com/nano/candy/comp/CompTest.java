package com.nano.candy.comp;
import com.nano.candy.ast.Program;
import com.nano.candy.comp.Checker;
import com.nano.candy.parser.ParserFactory;
import com.nano.common.io.FilePathUtils;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class CompTest {
	
	@Test public void testCoverage() throws IOException {
		for (File src : FilePathUtils.getFilesByPreOrder(new File("./test/comp_test"))) {
			if (src.getName().endsWith(".cd")) {
				Program program = ParserFactory.newParser(src).parse();
				Checker.check(program);
			}
		}
	}
}
