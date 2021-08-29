package com.nano.candy.ast.dumper;
import java.io.OutputStream;

public class DumperOptions {
	protected OutputStream os = System.out;
	protected boolean isDumpPosition = false;
	protected int indentWidth = -1;

	public void setOutStream(OutputStream os) {
		this.os = os;
	}
	
	public void setIsDumpPosition(boolean isDumpPosition) {
		this.isDumpPosition = isDumpPosition;
	}

	public boolean isDumpPosition() {
		return isDumpPosition;
	}

	public void setIndentWidth(int indentWidth) {
		this.indentWidth = indentWidth;
	}
	
	public String getIndent() {
		if (indentWidth < 0) {
			return "\t";
		}
		return " ".repeat(indentWidth);
	}
}
