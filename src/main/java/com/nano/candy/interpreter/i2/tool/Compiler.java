package com.nano.candy.interpreter.i2.tool;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.Program;
import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.interpreter.i2.builtin.type.error.CompilerError;
import com.nano.candy.interpreter.i2.builtin.type.error.IOError;
import com.nano.candy.interpreter.i2.code.CodeGenerator;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.vm.CompiledFileInfo;
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
				new CompilerError(String.format("\n%s\n", msg))
					.throwSelfNative();
			} catch (IOException e) {
				new CompilerError(e.getMessage())
					.throwSelfNative();
			}
		}
	}

	public static Chunk compileTree(ASTreeNode tree, InterpreterOptions options, boolean clearMsg) {
		checkLogger(clearMsg);
		return new CodeGenerator(options.isInteractionMode(), 
			options.isDebugMode()).genCode(tree);
	}
	
	public static Chunk compileChunk(String filePath, InterpreterOptions options, boolean clearMsg) {
		return compileChunk(new File(filePath), options, clearMsg);
	}
	
	public static Chunk compileChunk(File file, InterpreterOptions options, boolean clearMsg) {
		try {	
			Parser parser = ParserFactory.newParser(file);
			Program program = parser.parse();
			checkLogger(clearMsg);
			return compileTree(program, options, clearMsg);
		} catch (IOException e) {
			new IOError(file).throwSelfNative();
			return null;
		}
	}
	
	public static CompiledFileInfo compile(String filePath, InterpreterOptions options, boolean clearMsg) {
		return compile(new File(filePath), options, clearMsg);
	}
	
	public static CompiledFileInfo compile(File file, InterpreterOptions options,
										   boolean clearMsg) {
		Chunk chunk = compileChunk(file, options, clearMsg);
		return new CompiledFileInfo(
			file.getAbsolutePath(), chunk);
	}
	
	public static Chunk compileText(String text, InterpreterOptions options) {
		Parser parser = ParserFactory.newParser("Temp", text);
		Program program = parser.parse();
		checkLogger(false);
		return compileTree(program, options, false);
	}
}
