package com.nano.candy.tool;
import com.nano.candy.ast.Program;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.main.CandyOptions;
import com.nano.candy.parser.ParserFactory;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Options;
import com.nano.common.io.FileUtils;
import java.io.File;
import java.io.IOException;

public class ExeTool implements CandyTool {
	
	private static final Logger logger = Logger.getLogger();
	
	@Override
	public void defineOptions(Options options) {
		options.newGroup("Execute");
	}

	@Override
	public void run(Interpreter interpreter, CandyOptions options) throws Exception {
		File[] srcFiles = options.getFiles();
		if (srcFiles == null || srcFiles.length == 0) {
			interactivelyInterpret(interpreter);
			return;
		}
		interpret(interpreter, srcFiles);
	}

	private void interpret(Interpreter interpreter, File[] srcFiles) throws IOException {
		boolean isFailed = false;
		for (File sourceFile : srcFiles) {
			interpreter.initOrReset();
			boolean success = run(interpreter, sourceFile, false);
			isFailed = !success ? true : isFailed;
		}
		if (isFailed) {
			System.exit(1);
		}
		interpreter.onExit();
	}

	private void interactivelyInterpret(Interpreter interpreter) throws IOException {
		interpreter.initOrReset();
		StringBuilder input = new StringBuilder();
		boolean inMultiLineMode = false;
		java.util.Scanner scanner = new java.util.Scanner(System.in);
		while (true) {
			System.out.print(">>> ");
			if (!scanner.hasNext()) {
				return;
			}
			String line = scanner.nextLine().trim();
			if (line.endsWith("$")) {
				inMultiLineMode = !inMultiLineMode;
				line = line.substring(0, line.length() - 1);
			}
			input.append(line).append("\n");
			if (!inMultiLineMode) {
				run(interpreter, "command line", input.toString(), false, true);
				// clear saved input
				input.delete(0, input.length());
			}
		}
	}
	
	public static boolean run(Interpreter interpreter, 
	                          File sourceFile, 
	                          boolean exitIfError) throws IOException {
		return run(interpreter,
			sourceFile.getPath(), 
			FileUtils.readText(sourceFile), 
			exitIfError, 
			false
		);
	}
	
	public static boolean run(Interpreter interpreter, 
	                          String fileName, 
	                          String content, 
	                          boolean exitIfError, 
	                          boolean interactively) throws IOException
	{
		Program program = ParserFactory.newParser(fileName, content).parse();
		if (!logger.printAllMessage(exitIfError)) {
			return false;
		}
		interpreter.load(program, interactively);
		if (!logger.printAllMessage(exitIfError)) {
			return false;
		}
		return interpreter.run();
	}
}
