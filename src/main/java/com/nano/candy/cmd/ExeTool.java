package com.nano.candy.cmd;

import com.nano.candy.ast.Program;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.main.CandyOptions;
import com.nano.candy.parser.ParserFactory;
import com.nano.candy.std.Names;
import com.nano.candy.sys.CandySystem;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Options;
import com.nano.common.io.FileUtils;
import java.io.File;
import java.io.IOException;

public class ExeTool implements CandyTool {
	
	private static final Logger logger = Logger.getLogger();
	
	@Override
	public String groupName() {
		return "Executor";
	}

	@Override
	public String groupHelper() {
		return "Execute Candy source files.";
	}

	@Override
	public String[] aliases() {
		return new String[]{"exe", "ex"};
	}
	
	@Override
	public void defineOptions(Options options) {
		options.addOption("-m", false, 
			"Search source files from candy library directory.");
	}

	@Override
	public void run(Interpreter interpreter, CandyOptions options) throws Exception {
		File srcFile = options.getSourceFile();
		if (options.getCmd().hasOption("-m")) {
			CandySystem.checkEnv();
			srcFile = getSrcFile(srcFile, CandySystem.getCandyLibsPath());
		}
		CandySystem.checkSourceFile(srcFile);
		interpret(interpreter, srcFile);
	}
	
	private File getSrcFile(File userCmdArg, String fromPath) {
		File srcFile = new File(fromPath, userCmdArg.getPath());
		if (srcFile.isDirectory()) {
			srcFile = new File(srcFile, Names.MAIN_FILE_NAME);
		}
		return srcFile;
	}

	private void interpret(Interpreter interpreter, File srcFile) throws IOException {
		interpreter.initOrReset();
		int exitCode = run(interpreter, srcFile, false);
		System.exit(exitCode);
	}
	
	public static int run(Interpreter interpreter, 
	                          File sourceFile, 
	                          boolean exitIfError) throws IOException {
		return run(interpreter,
			sourceFile.getPath(), 
			FileUtils.readText(sourceFile), 
			exitIfError
		);
	}
	
	/**
	 * Return 65 exit code to represent user's input data was incorrect
	 * if fail to compile.
	 */
	public static int run(Interpreter interpreter, 
	                          String fileName, 
	                          String content, 
	                          boolean exitIfError) throws IOException
	{
		Program program = ParserFactory.newParser(fileName, content).parse();
		if (!logger.printAllMessage(exitIfError)) {
			return 65;
		}
		interpreter.load(program);
		if (!logger.printAllMessage(exitIfError)) {
			return 65;
		}
		return interpreter.run();
	}
}
