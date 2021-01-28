package com.nano.common.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class FileUtils {

	private FileUtils() {}

	static int bufferSize = 1024 ;

	public static void setBufferSize(int size) {
		bufferSize = Math.max(size, 126) ;
	}

	public static boolean exists(File file) {
		return file != null && file.exists() ;
	}

	public static boolean isDirectory(File file) {
		return file != null && file.isDirectory() ;
	}

	public static boolean isFile(File file) {
		return file != null && file.isFile() ;
	}

	private static boolean ensureWritable(File file, boolean isWriteIfExists) {
		if (file == null || file.isDirectory()) {
			return false ;
		}
		if (!file.exists()) {
			return createNewFile(file) ;
		}	
		return isWriteIfExists ;
	}

	public static boolean writeText(File file, String content) throws IOException {
		return writeText(file, content, true) ;
	}

	public static boolean writeText(File file, String content, boolean isWriteIfExists) throws IOException {
		return writeText(file, content, isWriteIfExists, false) ;
	}

	public static boolean writeText(File file, String content, boolean isWriteIfExists, boolean isAppend) throws IOException {
		if (!ensureWritable(file , isWriteIfExists)) {
			return false ;
		}

		Writer writer = null ;
		try {
			writer = new BufferedWriter(new FileWriter(file, isAppend), bufferSize) ;
			writer.write(content) ;
			writer.flush() ;
			writer.close() ;
			return true ;
		} finally {
			IOUtils.closesQuietly(writer) ;
		}
	}

	public static boolean writeInputStream(File file, InputStream is, boolean close) throws IOException {
		return writeInputStream(file, is, close, true, false) ;
	}

	public static boolean writeInputStream(File file, InputStream is, boolean close, boolean writeIfExist, boolean append) throws IOException {
		boolean res = writeByteArray(file, IOUtils.inputStreamToByteArray(is, bufferSize), writeIfExist, append) ;
		if (close) {
			is.close() ;
		}
		return res ;
	}

	public static boolean writeByteArray(File file, byte[] data) throws IOException {
		return writeByteArray(file, data, true, false) ;
	}

	public static boolean writeByteArray(File file, byte[] data, boolean writeIfExists, boolean append) throws IOException {
		if (!ensureWritable(file, writeIfExists)) {
			return false ;
		}
		BufferedOutputStream out = null ;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file, append), bufferSize) ;
			out.write(data) ;
			out.flush() ;
			return true ;
		} finally {
			IOUtils.closesQuietly(out) ;
		}
	}

	public static String readText(File file) throws IOException {
		Reader r = null ;
		try {
			r = new FileReader(file) ;
			String text = IOUtils.readerToString(r, bufferSize) ;
			return text ;
		} finally {
			IOUtils.closesQuietly(r) ;
		}
	}

	public static byte[] readByteArray(File file) throws IOException {
		FileInputStream is = null ;
		try {
			is = new FileInputStream(file) ;
			return IOUtils.inputStreamToByteArray(is) ;
		} finally {
			IOUtils.closesQuietly(is) ;
		}
	}

	public static boolean createNewFiles(File... files) {
		boolean result = true ;
		for (File file : files) {
			if (!createNewFile(file))
				result = false ;
		}
		return result ;
	}

	public static boolean createNewFile(String filePath) {
		if ("".equals(filePath.trim())) {
			return false ;
		}
		return createNewFile(new File(filePath)) ;
	}

	public static boolean createNewFile(File file) {
		try {
			if (file == null) {
				return false ;
			}
			if (file.isFile()) {
				return true ;
			}
			File parentFile = new File(file.getParent()) ;
			if (!parentFile.isDirectory()) {
				parentFile.mkdirs() ;
			}
			return file.createNewFile() ;
		} catch (IOException e) {}
		return false ;
	}

	public static String sizeFormat(long size) {
		double fileSize = size ;
		DecimalFormat format = new DecimalFormat("#.00") ;
		if (fileSize < 1024) {
			return fileSize + " b" ;
		} else if (fileSize < 1024 * 1024) {
			return format.format(fileSize / 1024) + " KB" ;
		} else if (fileSize < 1024 * 1024 * 1024) {
			return format.format(fileSize / (1024 * 1024)) + " MB" ;
		} else {
			return format.format(fileSize / (1024 * 1024 * 1024)) + " GB" ;
		}
	}


	public static void deleteFile(File file) {
		FilePathUtils.walkByPreOrder(file, f -> f.delete()) ;
	}

	private static void fastCopyFile(File srcFile, File destFile) throws IOException {
		InputStream in = null ;
		OutputStream out = null ;
		try {
			in = new BufferedInputStream(new FileInputStream(srcFile)) ;
			out = new BufferedOutputStream(new FileOutputStream(destFile)) ;
			IOUtils.inputStreamToOutputStream(in, out, bufferSize) ;
		} finally {
			IOUtils.closesQuietly(in, out) ;
		}
	}
	
	public static void copy(File src, File dest) throws IOException {
		FileUtils.copy(src, dest, true) ;
	}

	public static void copy(File src, File dest, boolean overwrite) throws IOException {
		if (!src.exists()) {
			throw new IOException("The source file doesn't exist: " + src.getAbsolutePath()) ;
		}
		
		if (dest.isFile()) {
			if (src.isDirectory()) {
				throw new IOException("Can't copy a directory to a file.") ;
			}
			fastCopyFile(src, dest) ;
			return ;
		}

		String srcRootPath = src.getParentFile().getAbsolutePath() ;
		String destRootPath = dest.getAbsolutePath() ;

		List<File> files = FilePathUtils.getFilesByBfsOrder(src) ;
		for (File file : files) {
			String relativePath = FilePathUtils.getRelativePathOf(
				file.getAbsolutePath(), 
				srcRootPath
			) ;
			File newFile = new File(destRootPath, relativePath) ;
			if (file.isDirectory()) {
				newFile.mkdirs() ;
			} else if (!src.exists() || overwrite) {
				src.createNewFile() ;
				fastCopyFile(file, newFile) ;
			}
		}
	}

	public static File move(File file, File moveToDir) {	
		File newFile = new File(moveToDir.getAbsolutePath(), file.getName()) ;
		return file.renameTo(newFile) ? newFile : null ;
	}

	public static long getDiskUsage(File fileOrDir) {
		if (fileOrDir.isFile()) {
			return fileOrDir.length() ;
		}
		long size = 0 ;
		for (File f : FilePathUtils.requiresNonNull(fileOrDir.listFiles())) {
			size += getDiskUsage(f) ;
		}
		return size ;
	}

	public static byte[] getFileMD5(File file) {
		return getFileSignature(file , "MD5") ;
	}

	public static byte[] getFileMD5(String filePath) {
		return getFileMD5(new File(filePath)) ;
	}

	public static String getFileMD5ToString(File file) {
		return getFileSignatureToString(file , "MD5") ;
	}

	public static String getFileMD5ToString(String filePath) {
		return getFileMD5ToString(new File(filePath)) ;
	}

	public static byte[] getFileSHA1(File file) {
		return getFileSignature(file , "SHA-1") ;
	}

	public static byte[] getFileSHA1(String filePath) {
		return getFileSHA1(new File(filePath)) ;
	}

	public static String getFileSHA1ToString(File file) {
		return getFileSignatureToString(file , "SHA-1") ;
	}

	public static String getFileSHA1ToString(String filePath) {
		return getFileSHA1ToString(new File(filePath)) ;
	}

	public static byte[] getFileSHA256(File file) {
		return getFileSignature(file , "SHA-256") ;
	}

	public static byte[] getFileSHA256(String filePath) {
		return getFileSHA256(new File(filePath)) ;
	}

	public static String getFileSHA256ToString(File file) {
		return getFileSignatureToString(file , "SHA-256") ;
	}

	public static String getFileSHA256ToString(String filePath) {
		return getFileSHA256ToString(new File(filePath)) ;
	}

	public static long getFileCRC32(File file) {
		CRC32 crc32 = new CRC32() ;
		FileInputStream fis = null ;
		CheckedInputStream cis = null ;
		long result = -1 ; 

		try {
			fis = new FileInputStream(file) ;
			cis = new CheckedInputStream(fis , crc32) ;
			while (cis.read() != -1) {}
			result = crc32.getValue() ;
		} catch (IOException e) {
		} finally {
			IOUtils.closesQuietly(fis, cis) ;
		}
		return result ;
	}

	public static long getFileCRC32(String filePath) {
		return getFileCRC32(new File(filePath)) ;
	}

	public static String getFileCRC32ToString(File file) {
		return Long.toHexString(getFileCRC32(file)) ;
	}

	public static String getFileCRC32ToString(String filePath) {
		return getFileCRC32ToString(new File(filePath)) ;
	}

	public static byte[] getFileSignature(File file, String algorithm) {
		FileInputStream fis = null ; 
		byte[] result = null ;
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm) ;
			fis = new FileInputStream(file) ;
			int len = 0 ;
			byte[] buffer = new byte[bufferSize] ;
			while ((len = fis.read(buffer)) > 0) {
				digest.update(buffer, 0, len) ;
			}
			result = digest.digest() ;
		} catch (Exception e) {
		} finally {
			IOUtils.closesQuietly(fis) ;
		}
		return result ;
	}

	public static String getFileSignatureToString(File file, String type) {
		byte[] digestByte = getFileSignature(file , type) ;
		if (digestByte == null) return null;
		return new BigInteger(1 , digestByte).toString(16) ;
	}

}
