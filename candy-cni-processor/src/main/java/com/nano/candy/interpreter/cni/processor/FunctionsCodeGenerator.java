package com.nano.candy.interpreter.cni.processor;

import java.io.IOException;
import java.util.List;
import javax.lang.model.element.VariableElement;

/**
 * Generating java code used to register candy functions.
 *
 * Note that this class only supports Java8 or more.
 */
public class FunctionsCodeGenerator extends CodeFileGenerator {
	
	@Override
	protected String getClassName(String qualifiedClassName) {
		return qualifiedClassName + "_FuncsRegister";
	}
	
	@Override
	protected void generateMethods(JavaFileWriter w, List<FunctionEnitity> funcs) throws IOException {
		w.declrMethod("public static", "JavaFunctionObj[]", "register", "");
		generateMethodBody(w, funcs);
		w.endBlock();
	}

	private void generateMethodBody(JavaFileWriter w, List<FunctionEnitity> funcs) throws IOException {
		w.writeStatement(
			"JavaFunctionObj[] funs = new JavaFunctionObj[%d]",
			funcs.size());
		int i = 0;
		for (FunctionEnitity f : funcs) {
			w.writeStatement(
				"funs[%d] = new JavaFunctionObj(\"%s\", %d, %d, %d, %s)",
				i, f.getName(), f.getArity(), 
				f.getVarArgIndex(), f.getOptionalArgFlags(),
				generateCallbackLambda(f));
			i ++;
		}
		w.writeStatement("return funs");
	}
	
	private static String generateCallbackLambda(FunctionEnitity method) {
		final List<? extends VariableElement> parameters = 
			method.getAnnotatedElement().getParameters();
		final int parametersSize = parameters.size();

		final StringBuilder callbackLambda = new StringBuilder("(env, opStack) -> {");

		for (int i = parametersSize-1; i >= 1; i --) {
			callbackLambda
				.append("\n            ")
				.append(parameters.get(i).asType().toString())
				.append(" arg" + (i-1))
				.append(" = ")
				.append(converArg(parameters.get(i).asType(), "opStack.pop()"))
				.append(";");
		}
		callbackLambda.append("\n           ")
			.append(String.format("return %s.%s(env",
					method.getQualifiedClassName(),
					method.getAnnotatedJavaMethodName()));
		for (int i = 0; i < parametersSize-1; i ++) {
			callbackLambda.append(", ").append("arg" + i);
		}
		callbackLambda.append(");");
		return callbackLambda.append("\n           }").toString();
	}
}
