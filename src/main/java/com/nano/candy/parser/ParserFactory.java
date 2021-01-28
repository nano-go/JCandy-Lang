package com.nano.candy.parser;

import com.nano.common.io.FileUtils;
import java.io.File;
import java.io.IOException;

public class ParserFactory {

	public static Parser newParser(File file) throws IOException {
		return newParser(file.getPath(), FileUtils.readText(file).toCharArray());
	}

	public static Parser newParser(String fileName, String input) {
		return newParser(fileName, input.toCharArray());
	}

	public static Parser newParser(String fileName, char[] input) {
		return newParser(ScannerFactory.newScanner(fileName, input)) ;
	}
	
	public static Parser newParser(Scanner scanner) {
		return new CandyParser(scanner);
	}
}
