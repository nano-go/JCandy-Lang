package com.nano.candy.interpreter.i2.cni;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

public class NativeLibraryLoader {
	
	public static NativeContext loadLibrary(String filePath, String className) throws IOException, ClassNotFoundException {
		return loadLibrary(new File(filePath), className);
	}
	
	public static NativeContext loadLibrary(File file, String className) throws IOException, ClassNotFoundException {
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
