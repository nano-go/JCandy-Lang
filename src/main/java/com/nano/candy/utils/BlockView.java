package com.nano.candy.utils;

public class BlockView {
	
	private static final String DIVIDER = "=".repeat(30);
	
	StringBuilder builder = new StringBuilder();
	public BlockView(){}
	
	private void addDivider(String name) {
		builder.append(DIVIDER)
			.append(" ").append(name).append(" ")
			.append(DIVIDER).append("\n");
	}
	
	public BlockView addBlock(String startName, String endName, String content) {
		addDivider(startName);
		builder.append(content);
		addDivider(endName);
		return this;
	}

	@Override
	public String toString() {
		return builder.toString();
	}
}
