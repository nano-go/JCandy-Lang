package com.nano.candy.cmd;
import com.nano.candy.cmd.CandyTool;
import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.sys.CandySystem;
import java.io.File;

public class CandyOptions {
	
	public static final void checkSourceFile(File srcFile) {
		if (srcFile == null) {
			throw new Options.ParseException("Missing source files.");
		}
		if (srcFile.isDirectory()) {
			throw new Options.ParseException
				("Can't open the file: " + srcFile.getPath());
		}
		if (!CandySystem.isCandySource(srcFile.getName())) {
			throw new Options.ParseException
				("Can't open a non-candy source file: " + srcFile.getPath());
		}
	}
	
	protected File srcFile;
	protected boolean printHelper;
	protected CandyTool tool;
	
	protected CommandLine cmdLine;
	protected Options options;
	protected InterpreterOptions interpreterOptions;
	
	protected CandyOptions() {}

	public void checkHasSrcFile() {
		checkSourceFile(srcFile);
	}
	
	public File getSourceFile() {
		return srcFile;
	}
	
	public CandyTool getTool() {
		return tool;
	}
	
	public CommandLine getCmd() {
		return cmdLine;
	}
	
	public InterpreterOptions getInterpreterOptions() {
		return interpreterOptions;
	}
	
	public boolean isPrintHelper() {
		return printHelper;
	}
	
	public void printHelper() {
		StringBuilder helper = new StringBuilder();
		helper.append("candy [tool] [-options] [source files...]\n\n");
		helper.append(options.helper());
		System.out.print(helper.toString());
	}
}
