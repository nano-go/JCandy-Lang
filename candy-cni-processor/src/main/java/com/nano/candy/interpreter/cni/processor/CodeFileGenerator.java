package com.nano.candy.interpreter.cni.processor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

import static com.nano.candy.interpreter.cni.processor.TypeNames.*;

public abstract class CodeFileGenerator {
	
	public static final HashMap<String, String> ARG_CONVERTOR = new HashMap<>();
	static {
		ARG_CONVERTOR.put(CANDY_OBJECT_TYPE, "%s");
		ARG_CONVERTOR.put(CANDY_DOUBLE_TYPE, "TypeError.requiresDoubleObj(%s)");
		ARG_CONVERTOR.put(CANDY_INTEGER_TYPE, "TypeError.requiresIntegerObj(%s)");
		ARG_CONVERTOR.put(CANDY_STRING_TYPE, "TypeError.requiresStringObj(%s)");
		ARG_CONVERTOR.put(CANDY_BOOL_TYPE, "TypeError.requiresBoolObj(%s)");
		ARG_CONVERTOR.put(CANDY_TUPLE_TYPE, "TypeError.requiresTupleObj(%s)");
		ARG_CONVERTOR.put(CANDY_ARRAY_TYPE, "TypeError.requiresArrayObj(%s)");
		ARG_CONVERTOR.put(CANDY_CALLABLE_TYPE, "TypeError.requiresCallable(%s)");

		ARG_CONVERTOR.put(byte.class.getName(), "(byte)ObjectHelper.asInteger(%s)");
		ARG_CONVERTOR.put(short.class.getName(), "(short)ObjectHelper.asInteger(%s)");
		ARG_CONVERTOR.put(int.class.getName(), "(int)ObjectHelper.asInteger(%s)");
		ARG_CONVERTOR.put(long.class.getName(), "(long)ObjectHelper.asInteger(%s)");
		ARG_CONVERTOR.put(float.class.getName(), "(float)ObjectHelper.asDouble(%s)");
		ARG_CONVERTOR.put(double.class.getName(), "ObjectHelper.asDouble(%s)");
		ARG_CONVERTOR.put(boolean.class.getName(), "(%s).boolValue(env)");
		ARG_CONVERTOR.put(String.class.getName(), "ObjectHelper.asString(%s)");
	}
	
	/**
	 * Converts a CandyObject into a primitive or a base Candy object by the
	 * specified type.
	 *
	 * <pre>
	 * public CandyObject foo(CNIEnv env, String str, ArrayObj arr) {...}
	 * 
	 * Converting the CandyObject (from the operand stack) into the String.
	 * Converting the CandyObject (from the operand stack) into the ArrayObj.
	 * </pre>
	 *
	 * You can write a Java method for Candy language by declaring built-in type
	 * or based-candy-object type parameters directly and the argument will be
	 * converted automatically to the corresponding paramerer type.
	 */
	public static String converArg(TypeMirror type, String arg) {
		return String.format(ARG_CONVERTOR.get(type.toString()), arg);
	}
	
	private static String getSimpleClassName(String fullClassName) {
		int index = fullClassName.lastIndexOf('.');
		if (index == -1) {
			return fullClassName;
		}
		return fullClassName.substring(index + 1);
	}
	
	public void generateCode(Elements elements, 
	                         Filer filer, String qualifiedClassName, 
	                         List<FunctionEnitity> funcs) throws IOException {
		String fullClassName = getClassName(qualifiedClassName);

		JavaFileObject javaFile = filer.createSourceFile(fullClassName);
		JavaFileWriter writer = new JavaFileWriter(javaFile.openWriter());
		try {
			TypeElement classElement = elements.getTypeElement(qualifiedClassName);
			generatePackageAndImports(writer, elements.getPackageOf(classElement));
			writer.declrClass("public", getSimpleClassName(fullClassName));
			generateMethods(writer, funcs);
			writer.endBlock();
		} finally {
			writer.close();
		}
	}

	protected void generatePackageAndImports(JavaFileWriter w, PackageElement packageOf) throws IOException {
		if (!packageOf.isUnnamed()) {
			w.writePackage(packageOf.getQualifiedName().toString());
		} else {
			w.writePackage("");
		}
		w.writeImport("com.nano.candy.interpreter.cni.*");
		w.writeImport("com.nano.candy.interpreter.builtin.CandyObject");
		w.writeImport("com.nano.candy.interpreter.builtin.utils.ObjectHelper");
		w.writeImport("com.nano.candy.interpreter.builtin.type.error.TypeError");
		w.write("\n");
	}
	
	protected abstract void generateMethods(JavaFileWriter writer, List<FunctionEnitity> funcs) throws IOException;
	protected abstract String getClassName(String qualifiedClassName);
	
}
