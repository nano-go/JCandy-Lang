package com.nano.candy.interpreter.cni.processor;

import java.io.IOException;
import java.util.List;
import javax.lang.model.element.VariableElement;

import static com.nano.candy.interpreter.cni.processor.TypeNames.*;

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
				"methods[%d] = new JavaMethodObj(className, \"%s\", %d, %d, %d, %s)",
				i, f.getName(), f.getArity(), 
				f.getVarArgIndex(), f.getOptionalArgFlags(),
				generateCallbackLambda(f));
			i ++;
		}
		w.writeStatement("return methods");
	}

	private static String generateCallbackLambda(FunctionEnitity method) {
		final List<? extends VariableElement> parameters = 
			method.getAnnotatedElement().getParameters();
		final int parametersSize = parameters.size();
		
		final StringBuilder callbackLambda = new StringBuilder("(env, instance, opStack) -> {");

		for (int i = parametersSize-1; i >= 1; i --) {
			callbackLambda
				.append("\n            ")
				.append("CandyObject ")
				.append(" arg" + (i-1))
				.append(" = opStack.pop();");
		}
		callbackLambda.append("\n            ")
			.append(String.format("return ((%s)instance).%s(env",
								  method.getQualifiedClassName(),
								  method.getAnnotatedJavaMethodName()));
		for (int i = 0; i < parametersSize-1; i ++) {
			callbackLambda.append(", ")
				.append(converArg(parameters.get(i+1).asType(), "arg" + i));
		}
		callbackLambda.append(");");
		return callbackLambda.append("\n           }").toString();
	}
}
