package com.nano.candy.cmd;
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
		CandySystem.checkSourceFile(getSourceFile());
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
