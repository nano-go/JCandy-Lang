package com.nano.candy.interpreter.i2.cni;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NativeMethodRegister {
	
	public static CNativeMethod[] generateNativeMethods(String className, Class clazz) {
		List<CNativeMethod> nativeMethods = new ArrayList<>();
		for (Method m : clazz.getDeclaredMethods()) {
			if (!m.isAnnotationPresent(NativeMethod.class)) {
				continue;
			}
			nativeMethods.add(generateNativeMethod(className, m));
		}
		return nativeMethods.toArray(new CNativeMethod[nativeMethods.size()]);
	}
	
	public static CNativeMethod generateNativeMethod(String className, Method m) {
		m.setAccessible(true);
		verifyNativeMethod(m);
		NativeMethod nativeMethod = m.getAnnotation(NativeMethod.class);
		String name = className + "." + nativeMethod.name();
		return new CNativeMethod(
			nativeMethod.name(), name, nativeMethod.argc(),
			nativeMethod.vaargIndex(), m);
	}
	
	private static void verifyNativeMethod(Method m) {
		AnnotationSigntureVerifier.verifyAnnotationPresent(m, NativeMethod.class);
		NativeMethod nativeMethod = m.getAnnotation(NativeMethod.class);
		AnnotationSigntureVerifier.verifyNativeMethod(
			m, nativeMethod.argc(), nativeMethod.vaargIndex(),
			nativeMethod.name());
	}
}
