package com.nano.candy.interpreter.builtin.utils;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.runtime.OperandStack;
import java.util.LinkedList;

public class ElementsUnpacker {
	
	/**
	 * Fetches n elements and unpacks them into the target elements and returns 
	 * it.
	 *
	 * <p>This method is used for calling functions.
	 *
	 * <p>For example:
	 * <pre>
	 * // Define a function, which the secondary parameter 'b' is a variable-length
	 * // argument.
	 * fun foo(a, *b) {...}
	 *
	 * var arr = [1, 2, 3]
	 * // Call it. We unpack the 'arr'.
	 * foo(1, *arr, 2)
	 * </pre>
	 *
	 * <p> In the above code part, we will use this `unpackFromStack` to
	 * unpack the argument {@code arr}, which is an array, and assign the unpacked
	 * arguments to the {@code a} and the {@code b}, which is a parameter that
	 * accepts variable-length arguments.
	 *
	 * @param n Fetchs n elements from the specified operand stack.
	 *
	 * @param starInqdex
	 *        {@code targetElements[startIndex]} will be fetched from the 
	 *        unpacked elements as far as possiable and combined into an array.
	 *        <p>For example:
	 *        <p>{@code a, *b = 1, 2, 3, 4;(starIndex = 1)} and {@code [2, 3, 4]}
	 *        will be assigned to the {@code b}.
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
												int unpackFlags,
												int optionalArgsFlags) 
	{
		if (!checkArgs(n, starIndex, targetLen, unpackFlags, optionalArgsFlags)) {
			return null;
		}
		if (unpackFlags == CallableObj.EMPTY_UNPACK_FLAGS) {
			// none of the elements to be unpacked.
			// fast get
			return fetchFromStack(opStack, n, starIndex, targetLen, optionalArgsFlags);
		}
		if ((starIndex < 0 || starIndex >= targetLen) && optionalArgsFlags == 0) {
			// none of variable-length arguments.
			// fast get
			CandyObject[] target = new CandyObject[targetLen];
			return unpackToTargetElements(env, opStack, n, unpackFlags, target) ?
				target : null;
		}
		return getTargetElements(
			env, opStack, n, starIndex, targetLen, unpackFlags, optionalArgsFlags);
	}
	
	private static CandyObject[] getTargetElements(CNIEnv env, OperandStack opStack,
												   int n,
												   int starIndex,
												   int targetLen,
											  	   int unpackFlags,
												   int optionlArgsFlags) {
		LinkedList<CandyObject> buffer = new LinkedList<>();
		unpackToBuffer(env, opStack, n, unpackFlags, buffer);
		int optionalParameterN = Integer.bitCount(optionlArgsFlags);
		// the number of the non-optional paramerers.
		// excluding variable-length argument.
		int nonOptionalParamN = targetLen-optionalParameterN;
		if (starIndex >= 0) nonOptionalParamN --;
		if (buffer.size() < nonOptionalParamN) {
			return null;
		}
		CandyObject[] target = new CandyObject[targetLen];
		for (int i = 0; i < targetLen; i ++) {
			target[i] = NullPointer.undefined();
		}
		int i;
		for (i = 0; i < targetLen && !buffer.isEmpty(); i ++) {
			if (i == starIndex) {
				target[i] = 
					fetchElementsAsFarAsPossiable(buffer, starIndex, targetLen);
			} else if (((optionlArgsFlags >> i) & 1) != 1) {
				target[i] = buffer.poll();
				nonOptionalParamN --;
			} else if (buffer.size() > nonOptionalParamN) {
				target[i] = buffer.poll();
			}
		}
		if (i <= starIndex) {
			target[starIndex] = ArrayObj.emptyArray();
		}
		if (unpackFlags == 0 && !buffer.isEmpty()) {
			return null;
		}
		return nonOptionalParamN <= 0 ? target : null;
	}

	private static CandyObject[] fetchFromStack(OperandStack opStack, int n, 
	                                            int starIndex,
	                                            int targetLen,
												int optionalArgFlags) {
		CandyObject[] target = new CandyObject[targetLen];
		if (starIndex < 0) {
			assignToTargetDirectly(opStack, n, target, optionalArgFlags);
			return target;
		}
		
		int noOptionalArgsN = target.length-Integer.bitCount(optionalArgFlags)-1;
		int nextTargetElements = targetLen-starIndex-1;
		int i;
		for (i = 0; i < starIndex; i ++) {
			if (((optionalArgFlags >> i) & 1) != 1) {
				target[i] = opStack.pop();
				n--;
				noOptionalArgsN --;
			} else if (n > noOptionalArgsN) {
				target[i] = opStack.pop();
				n--;
			} else {
				target[i] = NullPointer.undefined();
			}
		}
		// StarIndex Assign
		if (nextTargetElements >= n) {
			target[i] = ArrayObj.emptyArray();
		} else {
			ArrayObj arr = new ArrayObj(8);
			while (nextTargetElements < n) {
				arr.append(opStack.pop());
				n --;
			}
			target[i] = arr;
		}
		i ++;
		for (; i < targetLen; i ++) {
			target[i] = opStack.pop();
		}
		return target;
	}

	private static void assignToTargetDirectly(OperandStack opStack, int n, 
	                                           CandyObject[] target,
											   int optionalArgFlags) {
		if (optionalArgFlags == 0) {
			// n == targetLen
			for (int i = 0; i < n; i ++) {
				target[i] = opStack.pop();
			}
			return;
		}
		int noOptionalArgsN = target.length-Integer.bitCount(optionalArgFlags);
		for (int i = 0; i < target.length; i ++) {
			if (((optionalArgFlags >> i) & 1) != 1) {
				target[i] = opStack.pop();
				n--;
				noOptionalArgsN --;
			} else if (n > noOptionalArgsN) {
				target[i] = opStack.pop();
				n--;
			} else {
				target[i] = NullPointer.undefined();
			}
		}
	}
	
	/**
	 * Fetchs/Unpacks n elements from the stack into the specified target elements.
	 */
	public static boolean unpackToTargetElements(CNIEnv env, OperandStack opStack, 
	                                             int n, int unpackingBits,
	                                             CandyObject[] target) {
		int i ,j;
		outter: for (i = 0, j = 0; i < n && j < target.length; i ++) {
			if (((unpackingBits >> i) & 1) == 1) {
				CandyObject element = opStack.pop();
				if (element == NullPointer.nil()) {
					target[j ++] = element;
					continue;
				}
				for (CandyObject e : new IterableCandyObject(env, element)) {		
					target[j ++] = e;
					if (j >= target.length) {
						i ++;
						break outter;
					}
				}
			} else {
				target[j ++] = opStack.pop();
			}
		}
		for (;i < n; i ++) opStack.pop();
		return j >= target.length;
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
	 * Unpacks the specfied element into the buffer.
	 *
	 * <p>If the element is null, it will be added into the buffer directly.
	 *
	 * <p>We treat the specified element as an iterable object and use the
	 * {@link IterableCandyObject} to iterate over elements of the iterable
	 * object.
	 */
	public static void unpackElement(CNIEnv env, CandyObject element, 
	                                 LinkedList<CandyObject> buffer) {
		if (element == NullPointer.nil()) {
			buffer.offer(element);
			return;
		}
		for (CandyObject e : new IterableCandyObject(env, element)) {		
			buffer.offer(e);
		}
	}
	
	/**
	 * Fetchs elements from the buffer as far as possiable and 
	 * combined into an array.
	 *
	 * @param buffer      The element buffer.
	 * @param starIndex   The index of the variable-arguments.
	 * @param targetLen   The length of the target elements.
	 */
	public static ArrayObj fetchElementsAsFarAsPossiable(
	                                      LinkedList<CandyObject> buffer,
	                                      int starIndex, 
										  int targetLen)
	{
		// The number of the next target elements.
		// For example: a, *b, c, d; nextTargetElements = 2 (c and d)
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
	                                 int targetLen, 
									 int unpackingBits,
									 int optionalArgsFlags) {
		if (starIndex >= targetLen) {
			throw new Error
				("Unexpected Arguments: StarInxex(" + starIndex + 
			 	 "), TargetLen(" + targetLen + ")");
		}
		if (unpackingBits != CallableObj.EMPTY_UNPACK_FLAGS) {
			if (n == 0) {
				throw new Error("Invalid 'unpackingBits: " + unpackingBits);
			}
			return true;
		}
		if (optionalArgsFlags == 0) {
			return starIndex < 0 ? targetLen==n : n>=targetLen-1;
		}
		int optionalArgsCount = Integer.bitCount(optionalArgsFlags);
		if (starIndex < 0) {	
			return n >= targetLen-optionalArgsCount && n <= targetLen; 
		}
		return n >= targetLen-1-optionalArgsCount;
	}
}
