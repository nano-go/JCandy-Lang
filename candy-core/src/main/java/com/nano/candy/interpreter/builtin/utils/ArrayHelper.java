package com.nano.candy.interpreter.builtin.utils;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.BoolObj;
import com.nano.candy.interpreter.builtin.type.DoubleObj;
import com.nano.candy.interpreter.builtin.type.IntegerObj;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.cni.CNIEnv;

public class ArrayHelper {
	
	public static int hashCode(CNIEnv env, CandyObject[] arr, int from, int to) {
		int hash = 0;
		for (int i = from; i < to; i ++) {
			hash = hash * 31 + 
				(int) arr[i].callHashCode(env).intValue();
		}
		return hash;
	}
	
	public static String toString(CNIEnv env, CandyObject[] arr, String sperator,
								  CandyObject preventRecursion) {
		return toString(env, arr, 0, arr.length, sperator, preventRecursion);
	}
	
	public static String toString(CNIEnv env, CandyObject[] arr, int from, int to,
	                              String separator, CandyObject preventRecursion) {
		StringBuilder str = new StringBuilder();
		int i = from;
		int iMax = to-1;
		if (i <= iMax) {
			for (;;) {
				if (arr[i] == preventRecursion) {
					str.append(StringObj.RECURSIVE_LIST);
				} else {
					str.append(arr[i].callStr(env).value());
				}
				if (i >= iMax) {
					break;
				}
				i ++;
				str.append(separator);
			}
		}
		return str.toString();
	}
	
	public static Object[] toJavaArrayForFormat(CNIEnv env, ArrayObj arr) {
		Object[] args = new Object[arr.length()];
		int length = arr.length();
		for (int i = 0; i < length; i ++) {
			args[i] = toJavaTypeForFormat(env, arr.get(i));
		}
		return args;
	}

	public static Object toJavaTypeForFormat(CNIEnv env, CandyObject obj) {
		if (obj instanceof StringObj) {
			return ((StringObj)obj).value();
		}
		if (obj instanceof IntegerObj) {
			return ((IntegerObj)obj).intValue();
		}
		if (obj instanceof DoubleObj) {
			return ((DoubleObj)obj).doubleValue();
		}
		if (obj instanceof BoolObj) {
			return ((BoolObj)obj).value();
		}
		return obj.callStr(env);
	}
	
	public static ArrayObj toArray(CNIEnv env, CandyObject obj) {
		if (obj instanceof ArrayObj) {
			return (ArrayObj) obj;
		}
		return new ArrayObj(ObjectHelper.iterableObjToArray(env, obj));
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
