package com.nano.candy.interpreter.builtin.utils;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.runtime.OperandStack;
import com.nano.candy.std.Names;
import java.util.LinkedList;

public class ElementsUnpacker {
	
	/**
	 * Fetchs n elements and unpacks them into target elements and returns 
	 * them.
	 *
	 * <p>This method is used for calling functions.
	 *
	 * <p>For example:
	 * <pre>
	 * fun foo(a, *b) {...}
	 * foo(1, *arr)
	 * </pre>
	 *
	 * <p> In the above code part, we use this method to unpack the 
	 * argument {@code arr} which is an array and assign arguments to
	 * the {@code a} and {@code b} which is a variable argument.
	 *
	 * @param n Fetchs n elements from the specified operand stack.
	 *
	 * @param starIndex
	 *        {@code targetElements[startIndex]} will be fetched from the 
	 *        unpacked elements as far as possiable and combined into an array.
	 *        <p>For example:
	 *        <p>{@code a, *b = 1, 2, 3, 4;(starIndex = 1)} and {@code [2, 3, 4]}
	 *        will be assigned to {@code b}.
	 *
	 * @param targetLen The length of target elements
	 *
	 * @param unpackFlags
	 *        This is a bit-set (a vector of bits) which is used to specify
	 *        whether an source element(in the operand stack) needs to be 
	 *        unpacked or not.
	 *        <p>For example: For the expression {@code a, b = 1, *a, *b},
	 *        The {@code unpackFlags} is {@code 011(lower-bits)} which means
	 *        the {@code a} and the {@code b} will be unpacked.
	 *
	 * @return Target elements or null if unable to unpack.
	 */
	public static CandyObject[] unpackFromStack(CNIEnv env, OperandStack opStack,
	                                            int n,
	                                            int starIndex,
												int targetLen,
												int unpackFlags) 
	{
		if (!checkArgs(n, starIndex, targetLen, unpackFlags)) {
			return null;
		}
		if (unpackFlags == CallableObj.EMPTY_UNPACK_FLAGS) {
			return fetchFromStack(opStack, n, starIndex, targetLen);
		}
		
		if (starIndex < 0 || starIndex >= targetLen) {
			CandyObject[] elements = new CandyObject[targetLen];
			return unpackToElements(env, opStack, n, unpackFlags, elements) ?
				elements : null;
		}
		
		LinkedList<CandyObject> buffer = new LinkedList<>();
		unpackToBuffer(env, opStack, n, unpackFlags, buffer);
		if (buffer.size() < targetLen-1) {
			return null;
		}
		CandyObject[] elements = new CandyObject[targetLen];
		int i = 0;
		while (!buffer.isEmpty() && i < targetLen) {
			if (i == starIndex) {
				elements[i] = 
					fetchElementsAsFarAsPossiable(buffer, starIndex, targetLen);
			} else {
				elements[i] = buffer.poll();
			}
			i ++;
		}
		if (i == starIndex) {
			// the buffer is empty;
			elements[i ++] = ArrayObj.emptyArray();
		}
		return i < targetLen ? null : elements;
	}

	private static CandyObject[] fetchFromStack(OperandStack opStack, int n, 
	                                            int starIndex,
	                                            int targetLen) {
		CandyObject[] elements = new CandyObject[targetLen];
		if (starIndex < 0) { 
			// targetLen == n
			for (int i = 0; i < n; i ++) {
				elements[i] = opStack.pop();
			}
			return elements;
		}
		
		int nextTargetElements = targetLen - starIndex - 1;
		int i;
		for (i = 0; i < starIndex; i ++, n --) {
			elements[i] = opStack.pop();
		}
		// StarIndex Assign
		if (nextTargetElements >= n) {
			elements[i] = ArrayObj.emptyArray();
		} else {
			ArrayObj arr = new ArrayObj(8);
			while (nextTargetElements < n) {
				arr.append(opStack.pop());
				n --;
			}
			elements[i] = arr;
		}
		i ++;
		for (; i < targetLen; i ++, n --) {
			elements[i] = opStack.pop();
		}
		return elements;
	}
	
	/**
	 * Fetchs/Unpacks n elements from the stack into the elements.
	 */
	public static boolean unpackToElements(CNIEnv env, OperandStack opStack, 
	                                       int n, int unpackingBits,
	                                       CandyObject[] elements) {
		int i ,j;
		outter: for (i = 0, j = 0; i < n && j < elements.length; i ++) {
			if (((unpackingBits >> i) & 1) == 1) {
				CandyObject srcElement = opStack.pop();
				if (srcElement == NullPointer.nil()) {
					elements[j ++] = srcElement;
					continue;
				}
				for (CandyObject e : 
					new IterableCandyObject(env, srcElement)) {		
					elements[j ++] = e;
					if (j >= elements.length) {
						i ++;
						break outter;
					}
				}
			} else {
				elements[j ++] = opStack.pop();
			}
		}
		for (;i < n; i ++) opStack.pop();
		return j >= elements.length;
	}
	
	/**
	 * Fetchs/Unpacks n elements from the stack into the buffer.
	 */
	public static void unpackToBuffer(CNIEnv env, OperandStack opStack, 
	                                  int n, int unpackingBits,
	                                  LinkedList<CandyObject> buffer) {
		for (int i = 0; i < n; i ++) {
			if (((unpackingBits >> i) & 1) == 1) {
				unpackElement(env, opStack.pop(), buffer);
			} else {
				buffer.offer(opStack.pop());
			}
		}
	}
	
	/**
	 * Unpacks the specified element (must be an iterable object) into 
	 * the buffer. 
	 *
	 * <p>If the element is {@link NullPointer.nil()}, the {@code null} will
	 * not be unpacked, but it will be offered to the buffer.
	 */
	public static void unpackElement(CNIEnv env, CandyObject element, 
	                                 LinkedList<CandyObject> buffer) {
		if (element == NullPointer.nil()) {
			buffer.offer(element);
			return;
		}
		CandyObject obj = element.callIterator(env);
		CandyObject hasNext =
			obj.callGetAttr(env, Names.METHOD_ITERATOR_HAS_NEXT);
		CandyObject next =
			obj.callGetAttr(env, Names.METHOD_ITERATOR_NEXT);
		TypeError.checkIsCallable(hasNext);
		TypeError.checkIsCallable(next);
		int size = 0;
		while (((CallableObj) hasNext).call(env)
		       .boolValue(env).value()) {
			CandyObject e = ((CallableObj) next).call(env);
			buffer.offer(e);
			size ++;
		}
	}
	
	/**
	 * Fetchs elements from the buffer as far as possiable and 
	 * combined into an array.
	 */
	public static CandyObject fetchElementsAsFarAsPossiable(
	                                      LinkedList<CandyObject> buffer, 
	                                      int starIndex, 
										  int targetLen)
	{
		// The number of the next target elements.
		// For example: a, *b, c, d nextTargetElements = 2 (c and d)
		int nextTargetElements = targetLen - starIndex - 1;
		if (nextTargetElements >= buffer.size()) {
			return ArrayObj.emptyArray();
		}
		ArrayObj arr = new ArrayObj(8);
		while (nextTargetElements < buffer.size()) {
			arr.append(buffer.poll());
		}
		return arr;
	}
	
	private static boolean checkArgs(int n, int starIndex, 
	                                 int targetLen, int unpackingBits) {
		if (starIndex >= targetLen) {
			throw new Error
				("Unexpected Arguments: StarInxex(" + starIndex + 
			 	 "), TargetLen(" + targetLen + ")");
		}
		if (unpackingBits != 0) {
			return !(n == 0 && targetLen != 0);
		}
		if (starIndex < 0) {
			return targetLen == n;
		}
		return n >= targetLen-1;
	}
}
