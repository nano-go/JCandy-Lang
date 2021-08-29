package com.nano.candy.utils;
import java.util.Arrays;

public class ArrayUtils {
	
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
	public static final short[] EMPTY_SHORT_ARRAY = new short[0];
	public static final int[] EMPTY_INT_ARRAY = new int[0];
	public static final long[] EMPTY_LONG_ARRAY = new long[0];
	public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
	public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
	public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
	public static final char[] EMPTY_CHAR_ARRAY = new char[0];
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	
	
	private static final float CAPACITY_FACTOR = 1.5f;
	
	public static int[] growCapacity(int[] arr, int length) {
		if (length >= arr.length) {
			arr = Arrays.copyOf(arr, (int)(arr.length*CAPACITY_FACTOR));
		}
		return arr;
	}
	
	public static byte[] growCapacity(byte[] arr, int length) {
		if (length >= arr.length) {
			arr = Arrays.copyOf(arr, (int)(arr.length*CAPACITY_FACTOR));
		}
		return arr;
	}
	
	public static char[] growCapacity(char[] arr, int length) {
		if (length >= arr.length) {
			arr = Arrays.copyOf(arr, (int)(arr.length*CAPACITY_FACTOR));
		}
		return arr;
	}
	
	public static long[] growCapacity(long[] arr, int length) {
		if (length >= arr.length) {
			arr = Arrays.copyOf(arr, (int)(arr.length*CAPACITY_FACTOR));
		}
		return arr;
	}
	
	public static short[] growCapacity(short[] arr, int length) {
		if (length >= arr.length) {
			arr = Arrays.copyOf(arr, (int)(arr.length*CAPACITY_FACTOR));
		}
		return arr;
	}
	
	public static boolean[] growCapacity(boolean[] arr, int length) {
		if (length >= arr.length) {
			arr = Arrays.copyOf(arr, (int)(arr.length*CAPACITY_FACTOR));
		}
		return arr;
	}
	
	public static <T> T[] growCapacity(T[] arr, int length) {
		if (length >= arr.length) {
			arr = Arrays.copyOf(arr, (int)(arr.length*CAPACITY_FACTOR));
		}
		return arr;
	}
	
	public static <T> T[] mergeArray(T[] arr1, T[] arr2) {
		int oldlen = arr1.length;
		arr1 = Arrays.copyOf(arr1, arr1.length + arr2.length);
		System.arraycopy(arr2, 0, arr1, oldlen, arr2.length);
		return arr1;
	}
	
	public static byte[] mergeArray(byte[] arr1, byte[] arr2) {
		int oldlen = arr1.length;
		arr1 = Arrays.copyOf(arr1, arr1.length + arr2.length);
		System.arraycopy(arr2, 0, arr1, oldlen, arr2.length);
		return arr1;
	}
	
	public static short[] mergeArray(short[] arr1, short[] arr2) {
		int oldlen = arr1.length;
		arr1 = Arrays.copyOf(arr1, arr1.length + arr2.length);
		System.arraycopy(arr2, 0, arr1, oldlen, arr2.length);
		return arr1;
	}
	
	public static int[] mergeArray(int[] arr1, int[] arr2) {
		int oldlen = arr1.length;
		arr1 = Arrays.copyOf(arr1, arr1.length + arr2.length);
		System.arraycopy(arr2, 0, arr1, oldlen, arr2.length);
		return arr1;
	}
	
	public static long[] mergeArray(long[] arr1, long[] arr2) {
		int oldlen = arr1.length;
		arr1 = Arrays.copyOf(arr1, arr1.length + arr2.length);
		System.arraycopy(arr2, 0, arr1, oldlen, arr2.length);
		return arr1;
	}
	
	public static float[] mergeArray(float[] arr1, float[] arr2) {
		int oldlen = arr1.length;
		arr1 = Arrays.copyOf(arr1, arr1.length + arr2.length);
		System.arraycopy(arr2, 0, arr1, oldlen, arr2.length);
		return arr1;
	}
	
	public static double[] mergeArray(double[] arr1, double[] arr2) {
		int oldlen = arr1.length;
		arr1 = Arrays.copyOf(arr1, arr1.length + arr2.length);
		System.arraycopy(arr2, 0, arr1, oldlen, arr2.length);
		return arr1;
	}
	
	public static boolean[] mergeArray(boolean[] arr1, boolean[] arr2) {
		int oldlen = arr1.length;
		arr1 = Arrays.copyOf(arr1, arr1.length + arr2.length);
		System.arraycopy(arr2, 0, arr1, oldlen, arr2.length);
		return arr1;
	}
	
	public static byte[] repeat(byte[] arr, int count) {
		return repeat(arr, arr.length, count);
	}
	public static byte[] repeat(byte[] arr, int len, int count) {
		return repeat(arr, 0, len, count);
	}
	public static byte[] repeat(byte[] arr, int from, int len, int count) {
		if (count <= 0) {
			return EMPTY_BYTE_ARRAY;
		}
		if (len <= 0 || count == 1) return EMPTY_BYTE_ARRAY;
		byte[] newArr = new byte[len*count];
		for (int i = 0; i < newArr.length; i += len) {
			System.arraycopy(arr, from, newArr, i, len);
		}
		return newArr;
	}


	public static short[] repeat(short[] arr, int count) {
		return repeat(arr, arr.length, count);
	}
	public static short[] repeat(short[] arr, int len, int count) {
		return repeat(arr, 0, len, count);
	}
	public static short[] repeat(short[] arr, int from, int len, int count) {
		if (count <= 0) {
			return EMPTY_SHORT_ARRAY;
		}
		if (len <= 0 || count == 1) return EMPTY_SHORT_ARRAY;
		short[] newArr = new short[len*count];
		for (int i = 0; i < newArr.length; i += len) {
			System.arraycopy(arr, from, newArr, i, len);
		}
		return newArr;
	}


	public static int[] repeat(int[] arr, int count) {
		return repeat(arr, arr.length, count);
	}
	public static int[] repeat(int[] arr, int len, int count) {
		return repeat(arr, 0, len, count);
	}
	public static int[] repeat(int[] arr, int from, int len, int count) {
		if (count <= 0) {
			return EMPTY_INT_ARRAY;
		}
		if (len <= 0 || count == 1) return EMPTY_INT_ARRAY;
		int[] newArr = new int[len*count];
		for (int i = 0; i < newArr.length; i += len) {
			System.arraycopy(arr, from, newArr, i, len);
		}
		return newArr;
	}


	public static long[] repeat(long[] arr, int count) {
		return repeat(arr, arr.length, count);
	}
	public static long[] repeat(long[] arr, int len, int count) {
		return repeat(arr, 0, len, count);
	}
	public static long[] repeat(long[] arr, int from, int len, int count) {
		if (count <= 0) {
			return EMPTY_LONG_ARRAY;
		}
		if (len <= 0 || count == 1) return EMPTY_LONG_ARRAY;
		long[] newArr = new long[len*count];
		for (int i = 0; i < newArr.length; i += len) {
			System.arraycopy(arr, from, newArr, i, len);
		}
		return newArr;
	}


	public static float[] repeat(float[] arr, int count) {
		return repeat(arr, arr.length, count);
	}
	public static float[] repeat(float[] arr, int len, int count) {
		return repeat(arr, 0, len, count);
	}
	public static float[] repeat(float[] arr, int from, int len, int count) {
		if (count <= 0) {
			return EMPTY_FLOAT_ARRAY;
		}
		if (len <= 0 || count == 1) return EMPTY_FLOAT_ARRAY;
		float[] newArr = new float[len*count];
		for (int i = 0; i < newArr.length; i += len) {
			System.arraycopy(arr, from, newArr, i, len);
		}
		return newArr;
	}


	public static double[] repeat(double[] arr, int count) {
		return repeat(arr, arr.length, count);
	}
	public static double[] repeat(double[] arr, int len, int count) {
		return repeat(arr, 0, len, count);
	}
	public static double[] repeat(double[] arr, int from, int len, int count) {
		if (count <= 0) {
			return EMPTY_DOUBLE_ARRAY;
		}
		if (len <= 0 || count == 1) return EMPTY_DOUBLE_ARRAY;
		double[] newArr = new double[len*count];
		for (int i = 0; i < newArr.length; i += len) {
			System.arraycopy(arr, from, newArr, i, len);
		}
		return newArr;
	}


	public static char[] repeat(char[] arr, int count) {
		return repeat(arr, arr.length, count);
	}
	public static char[] repeat(char[] arr, int len, int count) {
		return repeat(arr, 0, len, count);
	}
	public static char[] repeat(char[] arr, int from, int len, int count) {
		if (count <= 0) {
			return EMPTY_CHAR_ARRAY;
		}
		if (len <= 0 || count == 1) return EMPTY_CHAR_ARRAY;
		char[] newArr = new char[len*count];
		for (int i = 0; i < newArr.length; i += len) {
			System.arraycopy(arr, from, newArr, i, len);
		}
		return newArr;
	}
	
	public static <T> T[] repeat(T[] arr, int count) {
		return repeat(arr, arr.length, count);
	}
	public static <T> T[] repeat(T[] arr, int len, int count) {
		return repeat(arr, 0, len, count);
	}
	public static <T> T[] repeat(T[] arr, int from, int len, int count) {
		if (count <= 0) {
			return Arrays.copyOf(arr, 0);
		}
		if (len <= 0 || count == 1) return Arrays.copyOf(arr, 0);
		T[] newArr = Arrays.copyOf(arr, count*len);
		int i = 0;
		if (from == 0) {
			i = len;
		}
		for (; i < newArr.length; i += len) {
			System.arraycopy(arr, from, newArr, i, len);
		}
		return newArr;
	}
}
