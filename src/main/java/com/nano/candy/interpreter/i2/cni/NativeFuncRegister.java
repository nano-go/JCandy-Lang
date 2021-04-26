package com.nano.candy.interpreter.i2.cni;
import com.esotericsoftware.reflectasm.MethodAccess;
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
	
	public static CNativeFunction[] getNativeFunctions(Class<?> nativeFunctionSet) {
		Method[] methods = getNativeMethods(nativeFunctionSet);
		return getNativeFunctions(nativeFunctionSet, methods);
	}

	private static CNativeFunction[] getNativeFunctions(Class<?> clazz, Method[] verifiedMethods) {
		CNativeFunction[] functions = new CNativeFunction[verifiedMethods.length];
		MethodAccess access = MethodAccess.get(clazz);
		for (int i = 0; i < verifiedMethods.length; i ++) {
			Method m = verifiedMethods[i];
			int index = access.getIndex(m.getName());
			NativeFunc anno = m.getAnnotation(NativeFunc.class);
			functions[i] = new CNativeFunction(
				anno.name(), anno.arity(), anno.varArgsIndex(),
				access, index
			);
		}
		return functions;
	}

	private static Method[] getNativeMethods(Class<?> nativeFunctionSet) {
		Method[] nativeMethods = nativeFunctionSet.getDeclaredMethods();
		List<Method> annotatedMethods = new ArrayList<>(nativeMethods.length);
		for (Method m : nativeMethods) {
			m.setAccessible(true);
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
			m, nativeFunc.arity(), nativeFunc.varArgsIndex(), nativeFunc.name());
	}
}
