package com.nano.candy.tool;

public class UnknownToolException extends Exception {
	
	public UnknownToolException(String name) {
		super("Unknown tool: " + name);
	}
}
