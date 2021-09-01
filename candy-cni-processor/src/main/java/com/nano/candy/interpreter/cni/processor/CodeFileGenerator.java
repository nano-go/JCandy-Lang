package com.nano.candy.interpreter.cni.processor;

import java.io.IOException;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

public abstract class CodeFileGenerator {
	
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
		w.write("\n");
	}
	
	protected abstract void generateMethods(JavaFileWriter writer, List<FunctionEnitity> funcs) throws IOException;
	protected abstract String getClassName(String qualifiedClassName);
	
}
