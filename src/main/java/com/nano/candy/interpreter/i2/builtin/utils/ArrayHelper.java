package com.nano.candy.interpreter.i2.builtin.utils;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.vm.VM;

public class ArrayHelper {
	
	public static int hashCode(VM vm, CandyObject[] arr, int from, int to) {
		int hash = 0;
		for (int i = from; i < to; i ++) {
			hash = hash * 31 + 
				(int) arr[i].callHashCode(vm).intValue();
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
				str.append(arr[i].callStr(vm).value());
				if (i >= iMax) {
					break;
				}
				i ++;
				str.append(separator);
			}
		}
		return str.toString();
	}
	
	public static ArrayObj toCandyStringArr(String[] arr) {
		ArrayObj candyArr = new ArrayObj(arr.length);
		for (String e : arr) {
			candyArr.append(StringObj.valueOf(e));
		}
		return candyArr;
	}
	
	public static ArrayObj toCandyIntegerArr(long[] arr) {
		ArrayObj candyArr = new ArrayObj(arr.length);
		for (long e : arr) {
			candyArr.append(IntegerObj.valueOf(e));
		}
		return candyArr;
	}

	public static ArrayObj toCandyIntegerArr(int[] arr) {
		ArrayObj candyArr = new ArrayObj(arr.length);
		for (int e : arr) {
			candyArr.append(IntegerObj.valueOf(e));
		}
		return candyArr;
	}

	public static ArrayObj toCandyIntegerArr(short[] arr) {
		ArrayObj candyArr = new ArrayObj(arr.length);
		for (short e : arr) {
			candyArr.append(IntegerObj.valueOf(e));
		}
		return candyArr;
	}

	public static ArrayObj toCandyIntegerArr(byte[] arr) {
		ArrayObj candyArr = new ArrayObj(arr.length);
		for (byte e : arr) {
			candyArr.append(IntegerObj.valueOf(e));
		}
		return candyArr;
	}

	public static ArrayObj toCandyDoubleArr(double[] arr) {
		ArrayObj candyArr = new ArrayObj(arr.length);
		for (double e : arr) {
			candyArr.append(DoubleObj.valueOf(e));
		}
		return candyArr;
	}
	
	public static ArrayObj toCandyDoubleArr(float[] arr) {
		ArrayObj candyArr = new ArrayObj(arr.length);
		for (double e : arr) {
			candyArr.append(DoubleObj.valueOf(e));
		}
		return candyArr;
	}
	
	public static ArrayObj toCandyBooleanArr(boolean[] arr) {
		ArrayObj candyArr = new ArrayObj(arr.length);
		for (boolean e : arr) {
			candyArr.append(BoolObj.valueOf(e));
		}
		return candyArr;
	}
}
