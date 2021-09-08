package com.nano.candy.interpreter;
import java.io.InputStream;
import java.io.PrintStream;

public class InterpreterOptions {
	
	private String[] args;
	private boolean isDebugMode;
	private boolean isInteractionMode;
	
	private InputStream stdin;
	private PrintStream stdout;
	private PrintStream stderr;
	
	public InterpreterOptions(String[] args) {
		this.args = args;
		this.stdin = System.in;
		this.stdout = System.out;
		this.stderr = System.err;
	}

	public InterpreterOptions setStderr(PrintStream stderr) {
		this.stderr = stderr;
		return this;
	}

	public PrintStream getStderr() {
		return stderr;
	}

	public InterpreterOptions setStdin(InputStream stdin) {
		this.stdin = stdin;
		return this;
	}

	public InputStream getStdin() {
		return stdin;
	}

	public InterpreterOptions setStdout(PrintStream stdout) {
		this.stdout = stdout;
		return this;
	}

	public PrintStream getStdout() {
		return stdout;
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
