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
}
