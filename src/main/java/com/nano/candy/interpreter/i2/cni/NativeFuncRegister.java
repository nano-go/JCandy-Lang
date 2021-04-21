package com.nano.candy.interpreter.i2.cni;
import com.nano.candy.interpreter.i2.rtda.FileScope;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NativeFuncRegister {
	
	public static void register(FileScope fileScope, Class<?> nativeFunctionSet) {
		for (CNativeFunction nativeFunc : getNativeFunctions(nativeFunctionSet)) {
			fileScope.defineCallable(nativeFunc);
		}
	}
	
	public static CNativeFunction generateNativeFunc(Method javaMethod) {
		verifyNativeMethod(javaMethod);
		NativeFunc anno = javaMethod.getAnnotation(NativeFunc.class);
		return new CNativeFunction(anno.name(), anno.arity(), javaMethod);
	}
	
	public static CNativeFunction[] getNativeFunctions(Class<?> nativeFunctionSet) {
		Method[] methods = getNativeMethods(nativeFunctionSet);
		return getNativeFunctions(methods);
	}

	private static CNativeFunction[] getNativeFunctions(Method[] verifiedMethods) {
		CNativeFunction[] functions = new CNativeFunction[verifiedMethods.length];
		for (int i = 0; i < verifiedMethods.length; i ++) {
			Method m = verifiedMethods[i];
			NativeFunc anno = m.getAnnotation(NativeFunc.class);
			functions[i] = new CNativeFunction(anno.name(), anno.arity(), m);
		}
		return functions;
	}

	private static Method[] getNativeMethods(Class<?> nativeFunctionSet) {
		Method[] nativeMethods = nativeFunctionSet.getDeclaredMethods();
		List<Method> annotatedMethods = new ArrayList<>(nativeMethods.length);
		for (Method m : nativeMethods) {
			if (!m.isAnnotationPresent(NativeFunc.class)) {
				continue;
			}
			verifyNativeMethod(m);
			annotatedMethods.add(m);
		}
		return annotatedMethods.toArray(new Method[0]);
	}
	
	private static void verifyNativeMethod(Method m) {
		AnnotationSigntureVerifier.verifyAnnotationPresent(m, NativeFunc.class);
		NativeFunc nativeFunc = m.getAnnotation(NativeFunc.class);
		AnnotationSigntureVerifier.verifyNativeFunc(
			m, nativeFunc.arity(), nativeFunc.name());
	}
}
