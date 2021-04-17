package com.nano.candy.interpreter;

public class InterpreterOptions {
	
	private String[] args;
	private boolean isDebugMode;
	private boolean isInteractionMode;
	
	public InterpreterOptions(String[] args) {
		this.args = args;
	}

	public String[] getArgs() {
		return args;
	}

	public InterpreterOptions setIsDebugMode(boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
		return this;
	}

	public boolean isDebugMode() {
		return isDebugMode;
	}

	public InterpreterOptions setIsInteractionMode(boolean isInteractionMode) {
		this.isInteractionMode = isInteractionMode;
		return this;
	}

	public boolean isInteractionMode() {
		return isInteractionMode;
	}
}
