package com.nano.candy.utils;
import java.util.Arrays;

public class ByteArray {
	private static final byte[] DEFUALT_ARRAY = new byte[0];
	
	private byte[] bytes;
	private int length;
	
	public ByteArray() {
		this(0);
	}
	
	public ByteArray(int capacity) {
		if (capacity <= 0) {
			this.bytes = DEFUALT_ARRAY;
		} else {
			this.bytes = new byte[capacity];
		}
	}
	
	private void ensureCapacity(int minSize) {
		if (this.bytes == DEFUALT_ARRAY) {
			this.bytes = new byte[16];
		}
		if (this.bytes.length < minSize) {
			int newLen;
			do {
				newLen = this.bytes.length * 2;
			} while (newLen < minSize);
			this.bytes = Arrays.copyOf(bytes, newLen);
		}
	}
	
	public ByteArray addByte(byte b) {
		ensureCapacity(length + 1);
		bytes[length ++] = b;
		return this;
	}
	
	public ByteArray setByte(int index, byte b) {
		bytes[index] = b;
		return this;
	}
	
	public byte getByte(int index) {
		return bytes[index];
	}
	
	public int length() {
		return length;
	}
	
	public byte[] getBytes() {
		return Arrays.copyOf(bytes, length);
	}
	
}
