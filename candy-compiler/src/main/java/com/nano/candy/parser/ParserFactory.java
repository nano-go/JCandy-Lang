package com.nano.candy.parser;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.utils.CandySourceFile;
import com.nano.candy.utils.Context;
import com.nano.candy.utils.Phase;
import com.nano.common.io.FileUtils;
import java.io.File;
import java.io.IOException;

public class ParserFactory {
	
	public static Phase<CandySourceFile, ASTreeNode> newPhase() {
		return new CandyParser();
	}
	
	public static Parser newParser(File file) throws IOException {
		return newParser(
			Context.getThreadLocalContext(), file);
	}

	public static Parser newParser(String fileName, String input) {
		return newParser(
			Context.getThreadLocalContext(), fileName, input);
	}

	public static Parser newParser(String fileName, char[] input) {
		return newParser(Context.getThreadLocalContext(), fileName, input);
	}

	public static Parser newParser(Scanner scanner) {
		return new CandyParser(Context.getThreadLocalContext(), scanner);
	}

	public static Parser newParser(Context context, File file) throws IOException {
		return newParser(context, file.getPath(), FileUtils.readText(file).toCharArray());
	}

	public static Parser newParser(Context context, String fileName, String input) {
		return newParser(context, fileName, input.toCharArray());
	}

	public static Parser newParser(Context context, String fileName, char[] input) {
		return newParser(context, 
			ScannerFactory.newScanner(context, fileName, input)) ;
	}
	
	public static Parser newParser(Context context, Scanner scanner) {
		return new CandyParser(context, scanner);
	}
}
