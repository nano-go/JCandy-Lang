package com.nano.candy.interpreter.cni.processor;

import java.io.IOException;
import java.util.List;

/**
 * Generating java code used to register candy methods.
 *
 * Note that this class only supports Java8 or more.
 */
public class MethodsCodeGenerator extends CodeFileGenerator {

	@Override
	protected String getClassName(String qualifiedClassName) {
		return qualifiedClassName + "_MetsRegister";
	}
	
	@Override
	protected void generateMethods(JavaFileWriter w, List<FunctionEnitity> methods) throws IOException {
		w.declrMethod(
			"public static", "JavaMethodObj[]", 
			"register", "String className");
		generateMethodBody(w, methods);
		w.endBlock();
	}

	private void generateMethodBody(JavaFileWriter w, List<FunctionEnitity> methods) throws IOException {
		w.writeStatement(
			"JavaMethodObj[] methods = new JavaMethodObj[%d]",
			methods.size());
		int i = 0;
		for (FunctionEnitity f : methods) {
			w.writeStatement(
				"methods[%d] = new JavaMethodObj(className, \"%s\", %d, %d, %s)",
				i, f.getName(), f.getArity(), f.getVarArgIndex(),
				generateCallbackLambda(f));
			i ++;
		}
		w.writeStatement("return methods");
	}

	private static String generateCallbackLambda(FunctionEnitity method) {
		return String.format(
			"(env, instance, args) -> ((%s) instance).%s(env, args)",
			method.getQualifiedClassName(), method.getAnnotatedJavaMethodName());
	}
}
