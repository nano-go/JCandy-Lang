package com.nano.candy.interpreter.cni.processor;

import java.io.Writer;
import java.io.IOException;

public class JavaFileWriter {
	
	private Writer w;
	private int indent;

	public JavaFileWriter(Writer w) {
		this.w = w;
	}
	
	public void write(String msg) throws IOException {
		for (int i = 0; i < indent; i ++) {
			w.write("    ");
		}
		w.write(msg);
	}
	
	private void format(String format, Object... args) throws IOException {
		write(String.format(format, args));
	}
	
	public void writePackage(String pkg) throws IOException {
		if (pkg.trim().length() != 0) {
			format("package %s;\n\n", pkg);
		}
	}
	
	public void writeImport(String pkg) throws IOException {
		format("import %s;\n", pkg);
	}
	
	public void writeStatement(String stmt) throws IOException {
		format("%s;\n", stmt);
	}
	
	public void writeStatement(String stmt, Object... args) throws IOException {
		format("%s;\n", String.format(stmt, args));
	}
	
	public void writeFor(String init, String condition, String increments) throws IOException {
		format("for (%s; %s; %s) {\n");
		indent ++;
	}
	
	public void writeForRange(String iName, int max) throws IOException {
		writeFor(
			String.format("int %s = 0", iName),
			String.format("%s < %d", iName, max),
			String.format("%s ++", iName));
		indent ++;
	}
	
	public void declrClass(String modifiers, String className) throws IOException {
		format("%s class %s {\n", modifiers, className);
		indent ++;
	}
	
	public void declrMethod(String modifiers, String retType, 
	                        String methodName,
	                        String params) throws IOException {
		format("%s %s %s(%s) {\n", modifiers, retType, methodName, params);
		indent ++;
	}
	
	public void endBlock() throws IOException {
		indent --;
		write("}\n");
	}
	
	public void close() throws IOException {
		w.close();
	}
}
