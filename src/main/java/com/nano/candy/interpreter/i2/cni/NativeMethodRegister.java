package com.nano.candy.interpreter.i2.cni;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.vm.VM;
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
			nativeMethods.add(generateNativeMethod(clazz, m));
		}
		return nativeMethods.toArray(new CNativeMethod[nativeMethods.size()]);
	}
	
	public static CNativeMethod generateNativeMethod(Class clazz, Method m) {
		verifyNativeMethod(m);
		String name = m.getName();
		MethodAccess method = MethodAccess.get(clazz);
		NativeMethod nativeMethod = m.getAnnotation(NativeMethod.class);
		String fullName = clazz.getSimpleName() + "." + nativeMethod.name();
		return new CNativeMethod(
			nativeMethod.name(), fullName, nativeMethod.argc(),
			nativeMethod.varArgsIndex(), method, 
			method.getIndex(name, VM.class, CandyObject[].class)
		);
	}
	
	private static void verifyNativeMethod(Method m) {
		AnnotationSigntureVerifier.verifyAnnotationPresent(m, NativeMethod.class);
		NativeMethod nativeMethod = m.getAnnotation(NativeMethod.class);
		AnnotationSigntureVerifier.verifyNativeMethod(
			m, nativeMethod.argc(), nativeMethod.varArgsIndex(),
			nativeMethod.name());
	}
}
