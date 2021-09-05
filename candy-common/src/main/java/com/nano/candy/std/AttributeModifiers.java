package com.nano.candy.std;

public class AttributeModifiers {
	
	/**
	 * The byte value represents the {@code public('pub')} modifier.
	 *
	 * <p> A public attribute can be accessed from any.
	 */
	public static final byte PUBLIC = 0;
	
	/**
	 * The byte value represents the {@code private('pri')} modifier.
	 *
	 * <p> A private attribute is only accessed from {@code this}.
	 */
	public static final byte PRIVATE = 1;
	
	/**
	 * The byte value represents the {@code read-only('reader')} modifier.
	 *
	 * <p> A read-only attribute can not be changed from any except
	 * {@code this}.
	 */
	public static final byte READ_ONLY = 1 << 1;
	
	/**
	 * The byte value represents the {@code write-only('writer')} modifier.
	 *
	 * <p> A write-only attribute can not be accessed from any except
	 * {@code this}
	 */
	public static final byte WRITE_ONLY = 1 << 2;
	
	/**
	 * The byte value represents the {@code builtin} modifier.
	 *
	 * <p> A builtin attribute is only accessed. Any can not change
	 * it, including {@code this}.
	 */
	public static final byte BUILTIN = 1 << 3;
	
	/**
	 * If the attribute of an object is accessed from {@code this} keyword, 
	 * the name of the attribute starts with the {@code '.'}.
	 *
	 * @param attr The name of the specified attribute.
	 *
	 * @return True if the name of the attribute is accessed from the
	 *  {@code this} keyword.
	 */
	public static boolean isAccessedFromThis(String attr) {
		return attr.length() >= 1 && attr.startsWith(".");
	}
	
	/**
	 * Removes the beginning {@code "."} of the specified attribure name.
	 */
	public static String getAttrNameIfAccessedFromThis(String attr) {
		return attr.substring(1);
	}
	
	public static String getAttrNameAccessedFromThis(String attr) {
		return "." + attr;
	}
	
	public static boolean isPublic(byte modifiers) {
		return (modifiers & PRIVATE) == 0;
	}

	public static boolean isPrivate(byte modifiers) {
		return (modifiers & PRIVATE) != 0;
	}

	public static boolean isReadOnly(byte modifiers) {
		return (modifiers & READ_ONLY) != 0;
	}

	public static boolean isWriteOnly(byte modifiers) {
		return (modifiers & WRITE_ONLY) != 0;
	}
	
	public static boolean isBuiltin(byte modifiers) {
		return (modifiers & BUILTIN) != 0;
	}
}
