package com.nano.candy.interpreter.i2.cni;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.rtda.FileScope;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
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

	protected static void verifyNativeMethod(Method m) {
		NativeFunc nativeFunc = m.getAnnotation(NativeFunc.class);
		if (nativeFunc == null) {
			error("The method %s must be annotated with NativeFunc.",
				m.getName());
		}
		if (nativeFunc.arity() < 0) {
			error("Invalid arity %d of the method %s.", 
				nativeFunc.arity(), m.getName());
		}
		if (!Names.isCandyIdentifier(nativeFunc.name())) {
			error("Invalid name %s of the method %s.", 
				nativeFunc.name(), m.getName());
		}	
		if (!Modifier.isStatic(m.getModifiers())) {
			error("The method %s must be static.", m.getName());
		}
		if (m.getParameterCount() != 2) {
			error("The method %s must have two paramters.", m.getName());
		}
		Parameter[] params = m.getParameters();
		if (params[0].getType() != VM.class) {
			error(
				"The first parameter of the method %s must receive a %s instance",
				m.getName(), VM.class.getSimpleName());
		}
		if (!params[1].getType().isArray() ||
		     params[1].getType().getComponentType() != CandyObject.class) {
			error("The second parameter of the method %s must receive a"
				+ " %s array.",
				m.getName(), CandyObject.class.getSimpleName());
		}
		if (m.getReturnType() != CandyObject.class) {
			error("The return type of the method '%s' must be %s",
				m.getName(), CandyObject.class.getSimpleName());
		}
	}

	private static void error(String fmt, Object... args) {
		throw new VerifyError(String.format(fmt, args));
	}
}
