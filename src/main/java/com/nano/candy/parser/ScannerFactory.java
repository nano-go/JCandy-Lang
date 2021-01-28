package com.nano.candy.parser;

import com.nano.common.io.FileUtils;
import java.io.File;
import java.io.IOException;

public class ScannerFactory {
	
	public static Scanner newScanner(File file) throws IOException {
		return newScanner(file.getPath(), FileUtils.readText(file).toCharArray()) ;
	}
	
	public static Scanner newScanner(String fileName, String input) {
		return newScanner(fileName, input.toCharArray()) ;
	}

	public static Scanner newScanner(String fileName, char[] input) {
		return new CandyScanner(fileName, input) ;
	}  
}
