package com.nano.candy.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CandySourceFile {
	
	private final String path;
	private final String content;
	private final boolean isRealFile;
	
	public CandySourceFile(File f) throws IOException {
		this.path = f.getPath();
		this.content = new String(Files.readAllBytes(f.toPath()));
		this.isRealFile = true;
	}
	
	public CandySourceFile(String name, String content) {
		this.path = name;
		this.content = content;
		this.isRealFile = false;
	}
	
	public CandySourceFile(String name, String content, boolean isRealFile) {
		this.path = name;
		this.content = content;
		this.isRealFile = isRealFile;
	}
	
	public String getPath() {
		return path;
	}

	public String getContent() {
		return content;
	}

	public boolean isRealFile() {
		return isRealFile;
	}
}
