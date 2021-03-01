package com.nano.candy.interpreter.i2.vm;

import com.nano.candy.interpreter.i2.instruction.Instructions;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.OperandStack;
import com.nano.candy.interpreter.i2.rtda.UpvalueObj;
import java.util.List;

public class DebugHelper {

	public static void traceFrameStack(Frame[] frameStack, int sp) {
		StringBuilder builder = new StringBuilder();
		builder.append("Frame Stack: ");
		for (int i = 0; i < sp; i ++) {
			builder.append("[ ").append(frameStack[i].name).append(" ] ");
		}
		builder.append("\n");
		System.out.print(builder.toString());
	}

	public static void traceOpenUpvalues(List<UpvalueObj> openUpvalues) {
		if (openUpvalues == null) {
			return;
		}
		StringBuilder openValuesStr = new StringBuilder();
		openValuesStr.append("Open Upvalues: ");
		for (UpvalueObj upvalueObj : openUpvalues) {
			openValuesStr.append("[ ")
				.append(upvalueObj.load())
				.append(":")
				.append(upvalueObj.index())
				.append(" ] ");
		}
		openValuesStr.append("\n");
		System.out.print(openValuesStr.toString());
	}

	public static void traceOperandStack(OperandStack opStack) {
		StringBuilder operandStack = new StringBuilder();
		operandStack.append("Operand Stack Trace: ");
		for (int i = opStack.size() -1; i >= 0; i --) {
			operandStack.append("[ ")
				.append(opStack.peek(i))
				.append(" ] ");
		}
		operandStack.append("\n");
		System.out.print(operandStack.toString());
	}

	public static void traceSlots(Frame frame) {
		StringBuilder slots = new StringBuilder();
		slots.append("Slots: ");
		final int LEN = frame.slotCount();
		for (int i = 0; i < LEN; i ++) {
			slots.append("[ ")
				.append(frame.load(i))
				.append(" ] ");
		}
		slots.append("\n");
		System.out.print(slots.toString());
	}

	public static void traceInstruction(int pc, byte[] code) {
		System.out.printf(
			"run->%d. %s\n", pc, Instructions.getName(code[pc])
		);
	}
}
