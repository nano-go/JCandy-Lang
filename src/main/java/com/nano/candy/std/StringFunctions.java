package com.nano.candy.std;

/**
 * Standard String functions.
 */
public class StringFunctions {
	
	public static String repeat(String str, long count) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i ++) {
			builder.append(str);
		}
		return builder.toString();
	}
	
	public static boolean equals(String str1, String str2) {
		return str1.equals(str2);
	}
	
	public static String valueOf(String str) {
		return str;
	}
	
	public static String valueOf(long l) {
		return String.valueOf(l);
	}
	
	public static String valueOf(double d) {
		return String.valueOf(d);
	}
	
	public static String valueOf(boolean b) {
		return b ? "true" : "false";
	}
	
	public static String nullStr() {
		return "null";
	}
}
