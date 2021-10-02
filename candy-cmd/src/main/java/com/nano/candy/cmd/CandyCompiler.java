package com.nano.candy.cmd;

import com.nano.candy.ast.Program;
import com.nano.candy.code.Chunk;
import com.nano.candy.codegen.CodeGenerator;
import com.nano.candy.parser.ParserFactory;
import com.nano.candy.utils.Context;
import com.nano.candy.utils.Logger;
import com.nano.common.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class CandyCompiler {
	
	public static Optional<Chunk> compile(File f, boolean printMsg) throws IOException {
		return compile(f.getAbsolutePath(), FileUtils.readText(f), printMsg);
	}
	
	public static Optional<Chunk> compile(String fileName, String content, boolean printMsg) throws IOException {
		Context c = new Context();
		Logger logger = c.get(Logger.class);
		Program program = ParserFactory.newParser(c, fileName, content).parse();
		if (!logger.printAllMessage(printMsg)) {
			return Optional.empty();
		}
		Chunk chunk = new CodeGenerator(false, false).genCode(program);
		if (!logger.printAllMessage(printMsg)) {
			return Optional.empty();
		}
		return Optional.of(chunk);
	}
}
