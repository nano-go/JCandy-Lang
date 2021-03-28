package com.nano.candy.sys;

import com.nano.common.io.FilePathUtils;
import java.io.File;

public class CandySystem {
	public static final String FILE_SUFFIX = "cd";
	public static final String END_OF_LINE = "end of line";
	
	public static final String DEFAULT_USER_DIR = getUserDirectory();
	public static final int DEFAULT_MAX_STACK = 1024 * 8;
	
	public static boolean isCandySource(String fileName) {
		return fileName.endsWith("." + FILE_SUFFIX);
	}
	
	public static File getCandySourceFile(String parent, String path) {
		String extension = FilePathUtils.getExtension(path);
		if (extension.length() != 0) {
			if (extension.equals(FILE_SUFFIX)) {
				return new File(parent, path);
			}
			return null;
		}
		return new File(parent, path + "." + FILE_SUFFIX);
	}
	
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
