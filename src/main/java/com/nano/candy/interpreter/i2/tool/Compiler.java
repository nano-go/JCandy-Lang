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
	
	private static boolean debugMode = false;
	
	public static void debugMode() {
		debugMode = true;
	}
	
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

	public static Chunk compileTree(ASTreeNode tree, boolean isInteractive, boolean debug,
	                                boolean clearMsg) throws CompilerError {
		Checker.check(tree);
		checkLogger(clearMsg);
		return new CodeGenerator(isInteractive, debug).genCode(tree);
	}
	
	public static Chunk compileChunk(String filePath, boolean debug, boolean clearMsg) throws CompilerError {
		return compileChunk(new File(filePath), debug, clearMsg);
	}
	
	public static Chunk compileChunk(File file, boolean debug, boolean clearMsg) throws CompilerError {
		try {	
			Parser parser = ParserFactory.newParser(file);
			Program program = parser.parse();
			checkLogger(clearMsg);
			return compileTree(program, false, debug, clearMsg);
		} catch (IOException e) {
			throw new FileError(file);
		}
	}
	
	public static CompiledFileInfo compile(String filePath, boolean clearMsg) throws CompilerError {
		return compile(filePath, Compiler.debugMode, clearMsg);
	}
	
	public static CompiledFileInfo compile(String filePath, boolean debug, boolean clearMsg) throws CompilerError {
		return compile(new File(filePath), debug, clearMsg);
	}
	
	public static CompiledFileInfo compile(File file, boolean clearMsg) throws CompilerError {
		return compile(file, Compiler.debugMode, clearMsg);
	}
	
	public static CompiledFileInfo compile(File file, boolean debug, boolean clearMsg) throws CompilerError {
		Chunk chunk = compileChunk(file, debug, clearMsg);
		return new CompiledFileInfo(
			file.getAbsolutePath(), chunk);
	}
}
