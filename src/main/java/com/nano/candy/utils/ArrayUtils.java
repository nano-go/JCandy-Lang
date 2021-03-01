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
	
	public static <T> T[] growCapacity(T[] arr, int length) {
		if (length >= arr.length) {
			arr = Arrays.copyOf(arr, (int)(arr.length*CAPACITY_FACTOR));
		}
		return arr;
	}
}
