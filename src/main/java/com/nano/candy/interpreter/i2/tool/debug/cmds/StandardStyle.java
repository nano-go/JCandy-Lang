package com.nano.candy.interpreter.i2.tool.debug.cmds;
import com.nano.candy.utils.StyleCode;

/**
 * BOLD -> about commands helper.
 * GRREN BOLD -> about names or number (variable, file name...).
 * CYAN BOLD -> about highlight.
 */
public class StandardStyle {
	
	public static String highlight(String str) {
		return StyleCode.render(str, StyleCode.CYAN, StyleCode.BOLD);
	}
	
	public static String namesOrNumber(Object obj) {
		return namesOrNumber(obj.toString());
	}
	
	public static String namesOrNumber(String str) {
		return StyleCode.render(str, StyleCode.GREEN, StyleCode.BOLD);
	}
	
	public static String cmd(String str) {
		return StyleCode.render(str, StyleCode.BOLD);
	}
}
