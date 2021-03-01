package com.nano.candy.utils;
import com.nano.candy.config.Config;
import java.io.File;
import java.io.FileFilter;

public class CandyFileFilter implements FileFilter {
	
	public static final CandyFileFilter CANDY_FILE_FILTER = new CandyFileFilter();
	
	private CandyFileFilter(){}
	@Override
	public boolean accept(File f) {
		return f.isFile() && f.getName().endsWith("." + Config.FILE_SUFFIX);
	}
}
