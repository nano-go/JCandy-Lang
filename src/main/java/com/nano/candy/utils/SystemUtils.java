package com.nano.candy.utils;

import java.io.File;

public class SystemUtils {
	
	public static final String DEFAULT_USER_DIR = getUserDirectory();

	public static void changeUserDirectory(String path) {
		File f = new File(path);
		String absPath;
		if (f.isFile()) {
			absPath = f.getParentFile().getAbsolutePath();
		} else {
			absPath = f.getAbsolutePath();
		}
		System.setProperty("user.dir", absPath);
	}
	
	public static String getUserDirectory() {
		return System.getProperty("user.dir");
	}
}
