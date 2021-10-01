package com.nano.candy.ast.dumper;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class DumperOptions {
	protected Writer out = new OutputStreamWriter(System.out);
	protected boolean isDumpPosition = false;
	protected String indent = "\t";

	public DumperOptions setOut(OutputStream os) {
		this.out = new OutputStreamWriter(os);
		return this;
	}
	
	public DumperOptions setOut(Writer w) {
		this.out = w;
		return this;
	}
	
	public Writer getOut() {
		return this.out;
	}
	
	public DumperOptions setIsDumpPosition(boolean isDumpPosition) {
		this.isDumpPosition = isDumpPosition;
		return this;
	}

	public boolean isDumpPosition() {
		return isDumpPosition;
	}

	public DumperOptions setIndent(String indent) {
		this.indent = indent;
		return this;
	}
	
	public String getIndent() {
		return indent;
	}
}
