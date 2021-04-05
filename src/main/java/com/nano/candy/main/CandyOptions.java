package com.nano.candy.main;
import com.nano.candy.tool.CandyTool;
import com.nano.candy.tool.CandyToolFactory;
import com.nano.candy.utils.CommandLine;
import com.nano.candy.utils.Options;
import java.io.File;

public class CandyOptions {
	
	protected File[] srcFiles;
	protected boolean printHelper;
	protected CandyTool tool;
	
	protected CommandLine cmdLine;
	protected Options options;
	
	protected CandyOptions() {}
	
	public void checkSrc() {
		if (srcFiles == null || srcFiles.length == 0) {
			throw new Options.ParseException("Missing source files.");
		}
	}
	
	public File[] getFiles() {
		return srcFiles;
	}
	
	public CandyTool getTool() {
		return tool;
	}
	
	public CommandLine getCmd() {
		return cmdLine;
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
