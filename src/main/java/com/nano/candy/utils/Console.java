package com.nano.candy.utils;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Console {
	
	private Scanner scanner;
	private PrintStream out;

	private String prefix = ">>> ";
	
	public Console(InputStream is, PrintStream ps) {
		this.scanner = new Scanner(is);
		this.out = ps;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public boolean hasInput() {
		return scanner.hasNext();
	}
	
	public String waitForUserInput() {
		out.print(prefix);
		if (!hasInput()) {
			return null;
		}
		return scanner.nextLine();
	}
	
	public PrintStream getPrinter() {
		return out;
	}
	
	public void printWithStyle(String str, StyleCode w, StyleCode fg, StyleCode bg) {
		out.print(StyleCode.render(str, w, fg, bg));
	}
	
	public void printWithStyle(String str, StyleCode fg, StyleCode bg) {
		out.print(StyleCode.render(str, fg, bg));
	}
	
	public void printWithStyle(String str, StyleCode bg) {
		out.print(StyleCode.render(str, bg));
	}
	
	public void printlnWithStyle(String str, StyleCode w, StyleCode fg, StyleCode bg) {
		out.println(StyleCode.render(str, w, fg, bg));
	}

	public void printlnWithStyle(String str, StyleCode fg, StyleCode bg) {
		out.println(StyleCode.render(str, fg, bg));
	}

	public void printlnWithStyle(String str, StyleCode bg) {
		out.println(StyleCode.render(str, bg));
	}
}
