package com.nano.candy.interpreter.cni;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

public class NativeLibraryLoader {
	
	public static NativeContext loadLibrary(String[] libraryPaths, 
	                                        String jarFilePath, 
											String className) throws IOException, ClassNotFoundException 
	{
		File f = findFile(libraryPaths, jarFilePath);
		return loadLibrary(f, className);
	}

	private static File findFile(String[] libraryPaths, String filePath) {
		for (String dirPath : libraryPaths) {
			if (dirPath != null) {
				File f = new File(dirPath, filePath);
				if (f.isFile()) return f;
			}
		}
		return new File(filePath);
	}
	
	private static NativeContext loadLibrary(File file, String className) throws IOException, ClassNotFoundException {
		URLClassLoader classLoader = new URLClassLoader(new URL[]{
			file.toURI().toURL(),
		}, Thread.currentThread().getContextClassLoader());
		Class<?> contextClass = classLoader.loadClass(className);
		return verifyClass(contextClass);
	}

	private static NativeContext verifyClass(Class<?> contextClass) {
		try {
			Constructor<?> constructor = contextClass.getConstructor();
			return (NativeContext) constructor.newInstance();
		} catch (Exception e) {
			throw new VerifyError(e.getMessage());
		}
	}
}
