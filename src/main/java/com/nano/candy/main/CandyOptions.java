package com.nano.candy.main;
import java.io.File;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class CandyOptions {
	
	public static final int PRINT_AST_BY_JSON_MASK = 1;
	
	protected File[] inputFiles;
	protected int printAstFlag;
	protected boolean printHelper;
	protected boolean interactively;

	protected Options options;
	
	protected CandyOptions() {}

	public boolean isInteractively() {
		return interactively;
	}
	
	public File[] getFiles() {
		return inputFiles;
	}
	
	public boolean isPrintAst() {
		return printAstFlag != 0;
	}
	
	public int getPrintAstFlag() {
		return printAstFlag;
	}
	
	public boolean isPrintHelper() {
		return printHelper;
	}
	
	public void printHelper() {
		HelpFormatter hf = new HelpFormatter();
		hf.setWidth(100);
		hf.setLeftPadding(2);
		hf.setSyntaxPrefix("  Usage: ");
		hf.printHelp("candy [-options] source files...", "  Options: ", options, "");
	}
}
