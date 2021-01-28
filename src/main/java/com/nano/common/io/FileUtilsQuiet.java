package com.nano.common.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;

public class FileUtilsQuiet {
	
	public static boolean writeText(File file, String content) {
		return writeText(file, content, true) ;
	}

	public static boolean writeText(File file, String content, boolean isWriteIfExists) {
		return writeText(file, content, isWriteIfExists, false) ;
	}

	public static boolean writeText(File file, String content, boolean isWriteIfExists, boolean isAppend) {
		try {
			return FileUtils.writeText(file, content, isWriteIfExists, isAppend) ;
		} catch (IOException e) {}
		return false ;
	}

	public static boolean writeInputStream(File file, InputStream is, boolean close) {
		return writeInputStream(file, is, close, true, false) ;
	}

	public static boolean writeInputStream(File file, InputStream is, boolean close, boolean writeIfExist, boolean append) {
		try {
			return FileUtils.writeInputStream(file, is, close, writeIfExist, append) ;
		} catch (IOException e) {}
		return false ;
	}

	public static boolean writeByteArray(File file, byte[] data) {
		return writeByteArray(file, data, true, false) ;
	}

	public static boolean writeByteArray(File file, byte[] data, boolean writeIfExists, boolean append) {
		try {
			return FileUtils.writeByteArray(file, data, writeIfExists, append) ;
		} catch (IOException e) {}
		return false ;
	}

	public static String readText(File file) {
		try {
			return FileUtils.readText(file) ;
		} catch (IOException e) {}
		return null ;
	}

	public static byte[] readByteArray(File file)  {
		try {
			return FileUtils.readByteArray(file) ;
		} catch (IOException e) {}
		return null ;
	}
	
	public static boolean copy(File src, File dest) {
		return copy(src, dest, true) ;
	}
	
	public static boolean copy(File src, File dest, boolean overwrite) {
		try {
			FileUtils.copy(src, dest, overwrite) ;
			return true ;
		} catch (IOException e) {}
		return false ;
	}
	
}
