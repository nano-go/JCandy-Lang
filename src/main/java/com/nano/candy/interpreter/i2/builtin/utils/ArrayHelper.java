package com.nano.candy.interpreter.i2.builtin.utils;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.vm.VM;

public class ArrayHelper {
	
	public static int hashCode(VM vm, CandyObject[] arr, int from, int to) {
		int hash = 0;
		for (int i = from; i < to; i ++) {
			hash = hash * 31 + 
				(int) arr[i].hashCodeApiExeUser(vm).intValue();
		}
		return hash;
	}
	
	public static String toString(VM vm, CandyObject[] arr, String sperator) {
		return toString(vm, arr, 0, arr.length, sperator);
	}
	
	public static String toString(VM vm, CandyObject[] arr, int from, int to,
	                              String separator) {
		StringBuilder str = new StringBuilder();
		int i = from;
		int iMax = to-1;
		if (i <= iMax) {
			for (;;) {
				str.append(arr[i].strApiExeUser(vm).value());
				if (i >= iMax) {
					break;
				}
				i ++;
				str.append(separator);
			}
		}
		return str.toString();
	}
}
