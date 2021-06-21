package com.nano.candy.std;
import com.nano.candy.utils.Characters;

public class Names {
	
	public static final String MOUDLE_FILE_NAME = "__module__.cd";
	public static final String MAIN_FILE_NAME = "main.cd";
	
	public static final String METHOD_INITALIZER = "init";
	
	public static final String METHOD_ITERATOR = "_iterator";
	
	public static final String METHOD_ITERATOR_HAS_NEXT = "_hasNext";
	public static final String METHOD_ITERATOR_NEXT = "_next";
	
	public static final String METHOD_HASH_CODE = "_hashCode";
	public static final String METHOD_EQUALS = "_equals";
	public static final String METHOD_STR_VALUE = "_str";
	
	public static final String METHOD_SET_ATTR = "_setAttr";
	public static final String METHOD_GET_ATTR = "_getAttr";
	public static final String METHOD_GET_UNKNOWN_ATTR = "_getUnknownAttr";
	
	public static final String METHOD_SET_ITEM = "_setItem";
	public static final String METHOD_GET_ITEM = "_getItem";
	
	public static final String METHOD_OP_POSITIVE = "_positive";
	public static final String METHOD_OP_NEGATIVE = "_negative";
	
	public static final String METHOD_OP_ADD = "_add";
	public static final String METHOD_OP_SUB = "_sub";
	public static final String METHOD_OP_MUL = "_mul";
	public static final String METHOD_OP_DIV = "_div";
	public static final String METHOD_OP_MOD = "_mod";
	
	public static final String METHOD_OP_GT   = "_gt";
	public static final String METHOD_OP_GTEQ = "_gteq";
	public static final String METHOD_OP_LT   = "_lt";
	public static final String METHOD_OP_LTEQ = "_lteq";
	
	private Names(){}
	
	public static boolean isCandyIdentifier(String name) {
		if (name.length() == 0) {
			return false;
		}
		if (!Characters.isCandyIdentifierStart(name.charAt(0))) {
			return false;
		}
		final int len = name.length();
		for (int i = 1; i < len; i ++) {
			char ch = name.charAt(i);
			if (!Characters.isCandyIdentifier(ch)) {
				return false;
			}
		}
		return true;
	}
}
