package com.nano.common.text;

public class StringUtils {
	
	public static boolean isEmpty(String str) {
		return str == null || "".equals(str.trim()) ;
	}
}
