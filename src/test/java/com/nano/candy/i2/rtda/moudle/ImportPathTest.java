package com.nano.candy.i2.rtda.moudle;

import com.nano.candy.interpreter.i2.rtda.module.ModuleManager;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;

public class ImportPathTest {
    
	private static final String[][] IMPORT_PATH_CASES = {
		{"parent", "/src.cd", "parent/src.cd"},
		{"parent", "/src", "parent/src.cd"},
		{"parent", "src.cd/test", "parent/src.cd/test.cd"},
		{"parent", "src.cd/test.cd", "parent/src.cd/test.cd"},
		{"parent", "src.cd/te.st", "parent/src.cd/te.st"},
		{"", "test.", "/test."},
		{"", ".", "/."},
		{"", "", "/"}
	};
	
	@Test public void testImportPath() {
		for (String[] paths : IMPORT_PATH_CASES) {
			File f = ModuleManager.findSourceFile(paths[0], paths[1]);
			Assert.assertEquals(paths[2], f.getPath());
		}
	}
}
