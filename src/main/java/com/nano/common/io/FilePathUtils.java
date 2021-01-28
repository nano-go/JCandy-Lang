package com.nano.common.io;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FilePathUtils {

	private static final File[] EMPTY_FILES = new File[0] ;

	protected static File[] requiresNonNull(File[] files) {
		return files == null ? EMPTY_FILES : files ;
	}
	
	protected static boolean isAccept(File f, FileFilter filter) {
		return filter == null || filter.accept(f) ;
	}

	public static String getRelativePathOf(File src, File of) {
		return getRelativePathOf(src.getAbsolutePath() , of.getAbsolutePath()) ;
	}

	public static String getRelativePathOf(String src, String of) {
		if (src.length() < of.length()) {
			return src ;
		}
		return src.indexOf(of) < 0 ? src : src.substring(of.length()) ;
	}

	public static boolean ensureParentExits(File file) {
		if (file.getParent() == "") {
			return true ;
		}
		return file.getParentFile().mkdirs() ;
	}
	
	public static File ensureValidFile(File parent, String name) {
		return new File(parent, ensureValidFileName(parent, name)) ;
	}

	public static File ensureValidFile(File file) {
		String name = ensureValidFileName(file.getParentFile(), file.getName()) ;
		return new File(file.getParentFile(), name) ;
	}
	
	public static String ensureValidFileName(File parent, String name) {
		int nth = 1 ;
		String nameWithoutExtension = getNameWithoutExtension(name) ;
		String extension = getExtension(name) ;
		File f = new File(parent, name) ;
		while (f.exists()) {
			name = nameWithoutExtension + "(" + nth + ")." + extension ;
			f = new File(parent, name) ;
		}
		return f.getName() ;
	}

	public static String getExtension(String filename) {
		int len = filename.length() ;
		if (len < 3) {
			return "" ;
		}
		int pointIndex = filename.lastIndexOf(".") ;
		if (pointIndex > 0) {
			return filename.substring(pointIndex + 1) ;
		}
		return "" ;
	}

	public static String getExtension(File file) {
		return getExtension(file.getName()) ; 
	}

	public static String getNameWithoutExtension(File file) {
		return getNameWithoutExtension(file.getName()) ;
	}

	public static String getNameWithoutExtension(String filename) {
		int len = filename.length() ;
		if (len < 3) {
			return filename ;
		}
		int pointIndex = filename.lastIndexOf(".") ;
		if (pointIndex > 0) {
			return filename.substring(0, pointIndex) ;
		}
		return filename ;
	}

	public static boolean containsExtension(File file) {
		if (file == null) return false ;
		return containsExtension(file.getName()) ;
	}

	public static boolean containsExtension(String filename) {
		if (filename == null) return false ;
		if (filename.length() < 3) return false ;
		return filename.lastIndexOf(".") > 0 ;
	}

	public static File rename(File file, String newName) {
		if (file == null || "".equals(newName)) {
			return null ;
		}
		File renameFile = new File(file.getParentFile() , newName) ;
		return file.renameTo(renameFile) ? renameFile : null ;
	}

	public static List<File> getFilesByBfsOrder(File file) {
		return getFilesByBfsOrder(file, null) ;
	}

	/**
	 * Returns all the files in a directory by BFS/level order.
	 *
	 * @param filter Nullable, a filter can filter out a directory but not its subfiles.
	 */
	public static List<File> getFilesByBfsOrder(File file, FileFilter filter) {
		ArrayList<File> list = new ArrayList<>() ;
		int i = 0 ;
		int size = 1 ;
		list.add(file) ;
		while (i < size) {
			File f = list.get(i ++) ;
			if (f.isDirectory()) {
				for (File child : requiresNonNull(f.listFiles())) {	
					list.add(child) ;
				}
			}
			size = list.size() ;
		}
		if (filter == null) {
			return list ;
		}
		return list.stream().filter(f -> filter.accept(f)).collect(Collectors.toList()) ;
	}
	
	public static void walkByBfsOrder(File file, Consumer<File> walkFunc) {
		walkByBfsOrder(file, null, walkFunc) ;
	}

	/**
	 * Travels recursively all the files in a directory by BFS/level order.
	 *
	 * @param filter Nullable, a filter can filter out a directory but not its subfiles.
	 */
	public static void walkByBfsOrder(File file, FileFilter filter, Consumer<File> walkFunc) {
		Queue<File> queue = new LinkedList<>() ;
		queue.offer(file) ;
		while (!queue.isEmpty()) {
			File f = queue.poll() ;		
			if (isAccept(f, filter)) {
				walkFunc.accept(f) ;
			}
			if (f.isDirectory()) {
				for (File child : requiresNonNull(f.listFiles())) {
					queue.offer(child) ;
				}
			}
		}
	}

	public static List<File> getFilesByPreOrder(File file) {
		return getFilesByPreOrder(file, null) ;
	}

	/**
	 * Returns all the files in a directory by pre order.
	 *
	 * @param filter Nullable, a filter can filter out a directory but not its subfiles.
	 */
	public static List<File> getFilesByPreOrder(File file, FileFilter filter) {
		ArrayList<File> list = new ArrayList<>() ;
		getFilesByPreOrderHelper(file, filter, list) ;
		return list ;
	}

	private static void getFilesByPreOrderHelper(File file, FileFilter filter, ArrayList<File> list) {
		if (file.isDirectory()) {
			for (File child : requiresNonNull(file.listFiles())) {
				getFilesByPreOrderHelper(child, filter, list) ;
			}
		}
		if (isAccept(file, filter)) {
			list.add(file) ;
		}
	}

	public static void walkByPreOrder(File file, Consumer<File> walkFunc) {
		walkByPreOrder(file, null, walkFunc) ;
	}

	/**
	 * Travels recursively all the files in a directory by pre order.
	 *
	 * @param filter Nullable, a filter can filter out a directory but not its subfiles.
	 */
	public static void walkByPreOrder(File file, FileFilter filter, Consumer<File> walkFunc) {
		if (file.isDirectory()) {
			for (File child : requiresNonNull(file.listFiles())) {
				walkByPreOrder(child, filter, walkFunc) ;
			}
		}
		if (isAccept(file, filter)) {
			walkFunc.accept(file) ;
		}
	}

}
