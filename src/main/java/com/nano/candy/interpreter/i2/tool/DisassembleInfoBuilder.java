package com.nano.candy.interpreter.i2.tool;

class DisassembleInfoBuilder {
	
	private StringBuilder builder;

	private String indentStr;
	private int indent;

	public DisassembleInfoBuilder() {
		builder = new StringBuilder();
		indentStr = "    ";
	}

	public String indent() {
		return indentStr.repeat(indent);
	}

	public DisassembleInfoBuilder enterBlock(String format, Object... args) {
		builder.append(indent()).append(String.format(format, args)).append(":\n");
		indent ++;
		return this;
	}

	public DisassembleInfoBuilder endBlock() {
		indent --;
		builder.append(indent()).append("end\n");
		return this;
	}

	public DisassembleInfoBuilder appendIndent() {
		builder.append(indent());
		return this;
	}

	public DisassembleInfoBuilder appendf(String format, Object... args) {
		builder.append(String.format(format, args));
		return this;
	}

	public DisassembleInfoBuilder append(Object obj) {
		builder.append(obj);
		return this;
	}

	public DisassembleInfoBuilder append(String str) {
		builder.append(str);
		return this;
	}

	public DisassembleInfoBuilder append(byte b) {
		builder.append(b);
		return this;
	}

	@Override
	public String toString() {
		return builder.toString();
	}
	
}
