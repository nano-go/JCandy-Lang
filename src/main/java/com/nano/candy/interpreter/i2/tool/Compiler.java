package com.nano.candy.interpreter.i2.tool;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.Program;
import com.nano.candy.comp.Checker;
import com.nano.candy.interpreter.i2.codegen.CodeGenerator;
import com.nano.candy.interpreter.i2.error.CompilerError;
import com.nano.candy.interpreter.i2.error.FileError;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.moudle.CompiledFileInfo;
import com.nano.candy.parser.Parser;
import com.nano.candy.parser.ParserFactory;
import com.nano.candy.utils.Logger;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

public class Compiler {
	
	private static void checkLogger(boolean clearMsg) {
		Logger logger = Logger.getLogger();
		if (logger.hadErrors()) {
			try {
				StringWriter writer = new StringWriter();
				logger.printErrors(writer);
				if (clearMsg) {
					logger.clearErrors();
				}
				String msg = writer.toString().trim();
				writer.close();
				throw new CompilerError(String.format("\n%s\n", msg));
			} catch (IOException e) {
				throw new CompilerError(e.getMessage());
			}
		}
	}

	public static Chunk compileTree(ASTreeNode tree, boolean isInteractive, 
	                                boolean clearMsg) throws CompilerError {
		Checker.check(tree);
		checkLogger(clearMsg);
		return new CodeGenerator(isInteractive).genCode(tree);
	}
	
	public static Chunk compileChunk(String filePath, boolean clearMsg) throws CompilerError {
		return compileChunk(new File(filePath), clearMsg);
	}
	
	public static Chunk compileChunk(File file, boolean clearMsg) throws CompilerError {
		try {	
			Parser parser = ParserFactory.newParser(file);
			Program program = parser.parse();
			checkLogger(clearMsg);
			return compileTree(program, false, clearMsg);
		} catch (IOException e) {
			throw new FileError(file);
		}
	}
	
	public static CompiledFileInfo compile(String filePath, boolean clearMsg) throws CompilerError {
		return compile(new File(filePath), clearMsg);
	}
	
	public static CompiledFileInfo compile(File file, boolean clearMsg) throws CompilerError {
		Chunk chunk = compileChunk(file, clearMsg);
		return new CompiledFileInfo(
			file.getAbsolutePath(), chunk);
	}
}
