package com.nano.candy.utils;
import java.util.Arrays;

public class ArrayUtils {
	
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
}
