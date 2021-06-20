package com.nano.candy.main;
import com.nano.candy.cmd.CandyTool;
import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.sys.CandySystem;
import com.nano.candy.utils.CommandLine;
import com.nano.candy.utils.Options;
import java.io.File;

public class CandyOptions {
	
	protected File srcFile;
	protected boolean printHelper;
	protected CandyTool tool;
	
	protected CommandLine cmdLine;
	protected Options options;
	protected InterpreterOptions interpreterOptions;
	
	protected CandyOptions() {}

	public void checkHasSrcFile() {
		if (srcFile == null) {
			throw new Options.ParseException("Missing source files.");
		}
		if (srcFile.isDirectory()) {
			throw new Options.ParseException("Can't open a directory.");
		}
		if (!CandySystem.isCandySource(srcFile.getName())) {
			throw new Options.ParseException
				("Can't open non-candy source file.");
		}
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
