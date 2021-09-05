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
	 * Fetchs elements from the stack to the target elements.
	 *
	 * @param n
	 *        Fetchs n elements from the stack in the vm.
	 *
	 * @param starIndex
	 *        The index in the target elements will greedily fetch 
	 *        elements from the buffer as an array.
	 *        For example: {@code a, *b = 1, 2, 3, 4;}
	 *        The {@code b} will be assigned as the array {@code [1, 2, 3]}.
	 *
	 * @param targetLen
	 *        The length of the returned array.
	 *
	 * @param unpackFlags
	 *        This is a bit-set of the elements in the stack. If the bit
	 *        of the index corresponding the element in the stack is
	 *        true (1), it will be unpacked (iter) to the buffer.
	 *        For example: {@code callMe(*a, *b, c);}
	 *        The bit-set is 0...110. It means that the {@code a} and the
	 *        {@code b} will be unpacked.
	 *
	 * @return the target elements or null if fail to unpack.
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
					greedilyFetchElements(buffer, starIndex, targetLen);
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
	 * Unpacks the specified element (must be a iterable object) into 
	 * the buffer. 
	 *
	 * If the element is {@link NullPointer.nil()}, the {@code null} will
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
	 * Fetchs greedily the elements from the buffer as the array and 
	 * returns it. If the buffer has not enough elements to fetch, 
	 * it returns an empty Candy array.
	 */
	public static CandyObject greedilyFetchElements(LinkedList<CandyObject> buffer, 
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