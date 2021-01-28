package com.nano.common.io ;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;

public class IOUtils {

	private IOUtils() {}

	public static void closesQuietly(Closeable... closeable) {
		for (Closeable c : closeable) {
			if (c == null) continue ;
			try {
				c.close() ;
			} catch (IOException e) {}
		}
	}

	public static byte[] inputStreamToByteArray(InputStream is) throws IOException {
		return inputStreamToByteArray(is, 1024) ;
	}
	
	public static byte[] inputStreamToByteArray(InputStream is , int bufferSize) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream() ;
		byte[] buffer = new byte[bufferSize] ;
		int len = 0 ;
		if ((len = is.read(buffer)) > 0) {
			baos.write(buffer, 0, len) ;
		}
		return baos.toByteArray() ;	
	}

	public static void inputStreamToOutputStream(InputStream is , OutputStream os) throws IOException {
		inputStreamToOutputStream(is, os, 1024) ;
	}

	public static void inputStreamToOutputStream(InputStream is , OutputStream os , int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize] ;
		int len = 0 ;
		while ((len = is.read(buffer)) > 0) {
			os.write(buffer , 0 , len) ;
		}
		os.flush() ;
	}

	public static String inputStreamToString(InputStream is) throws IOException {
		return inputStreamToString(is, 1024) ;
	}

	public static String inputStreamToString(InputStream is, int bufferSize) throws IOException {
		return inputStreamToString(is, bufferSize, "UTF-8") ;
	}

	public static String inputStreamToString(InputStream is, int bufferSize, String charset) throws IOException {
		return readerToString(new InputStreamReader(is, charset), bufferSize) ;
	}

	public static String readerToString(Reader reader, int bufferSize) throws IOException {
		char[] buffer = new char[bufferSize] ;
		StringBuilder str = new StringBuilder() ;
		int len = 0 ;
		while ((len = reader.read(buffer)) > 0) {
			str.append(buffer, 0, len) ;
		}
		return str.toString() ;
	}

	public static String throwableToString(Throwable e) {
		StringWriter stringWrite = new StringWriter() ;
		PrintWriter printWrite = new PrintWriter(stringWrite) ;
		e.printStackTrace(printWrite) ;
		closesQuietly(printWrite) ;
		return stringWrite.toString() ;
	}

}
