package com.nano.candy.std;
import com.nano.common.text.StringUtils;

public class Names {
	
	public static final String METHOD_INITALIZER = "init";
	
	public static final String METHOD_ITERATOR = "_iterator";
	
	public static final String METHOD_ITERATOR_HAS_NEXT = "_hasNext";
	public static final String METHOD_ITERATOR_NEXT = "_next";
	
	public static final String METHOD_HASH_CODE = "_hashCode";
	public static final String METHOD_EQUALS = "_equals";
	public static final String METHOD_STR_VALUE = "_str";
	
	public static final String METHOD_SET_ATTR = "_setAttr";
	public static final String METHOD_GET_ATTR = "_getAttr";
	
	public static final String METHOD_SET_ITEM = "_setItem";
	public static final String METHOD_GET_ITEM = "_getItem";
	
	public static final String METHOD_OP_POSITIVE = "_postive";
	public static final String METHOD_OP_NEGATIVE = "_neg";
	
	public static final String METHOD_OP_ADD = "_add";
	public static final String METHOD_OP_SUB = "_sub";
	public static final String METHOD_OP_MUL = "_mul";
	public static final String METHOD_OP_DIV = "_div";
	public static final String METHOD_OP_MOD = "_mod";
	
	public static final String METHOD_OP_GT   = "_gt";
	public static final String METHOD_OP_GTEQ = "_gteq";
	public static final String METHOD_OP_LT   = "_lt";
	public static final String METHOD_OP_LTEQ = "_lteq";
	
	public static String methodName(String methodName) {
		if (StringUtils.isEmpty(methodName)) {
			return METHOD_INITALIZER;
		}
		return methodName;
	}
}
