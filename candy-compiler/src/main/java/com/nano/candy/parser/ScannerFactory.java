package com.nano.candy.parser;

import com.nano.candy.utils.Context;
import com.nano.common.io.FileUtils;
import java.io.File;
import java.io.IOException;

public class ScannerFactory {
	
	public static Scanner newScanner(File file) throws IOException {
		return newScanner(Context.getThreadLocalContext(), file);
	}

	public static Scanner newScanner(String fileName, String input) {
		return newScanner(Context.getThreadLocalContext(), fileName, input);
	}

	public static Scanner newScanner(String fileName, char[] input) {
		return new CandyScanner(Context.getThreadLocalContext(), false, fileName, input);
	}
	
	public static Scanner newScanner(Context context, File file) throws IOException {
		return newScanner(context, file.getPath(), FileUtils.readText(file));
	}
	
	public static Scanner newScanner(Context context, String fileName, String input) {
		return newScanner(context, fileName, input.toCharArray());
	}

	public static Scanner newScanner(Context context, String fileName, char[] input) {
		return new CandyScanner(context, false, fileName, input);
	}
}
