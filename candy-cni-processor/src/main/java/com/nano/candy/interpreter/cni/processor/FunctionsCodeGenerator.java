package com.nano.candy.interpreter.cni.processor;

import java.io.IOException;
import java.util.List;

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
				"funs[%d] = new JavaFunctionObj(\"%s\", %d, %d, %s)",
				i, f.getName(), f.getArity(), f.getVarArgIndex(),
				// Java8 method reference.
				f.getQualifiedClassName() + "::" + f.getAnnotatedJavaMethodName());
			i ++;
		}
		w.writeStatement("return funs");
	}
	
}
