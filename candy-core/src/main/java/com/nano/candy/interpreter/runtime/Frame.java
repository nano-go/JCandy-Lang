package com.nano.candy.interpreter.runtime;

import com.nano.candy.code.Chunk;
import com.nano.candy.code.CodeAttribute;
import com.nano.candy.code.ConstantValue;
import com.nano.candy.code.ErrorHandlerTable;
import com.nano.candy.interpreter.builtin.type.PrototypeFunction;

public final class Frame {
	
	public static Frame fetchFrame(PrototypeFunction prototypeFunc, 
								   OperandStack opStack) {	
		return new Frame(opStack, prototypeFunc);
	}
	
	/**
	 * True when the virtual machine executes the RETURN opcode, 
	 * the virtual machine returns from the eval method.
	 *
	 * This method is used to call Candy functions from Java.
	 */
	protected boolean exitRunAtReturn;
	
	protected int pc;
	protected PrototypeFunction closure;
	
	/**
	 * +------------------------+
	 * |                        |
	 * |        Operands        |   <- push pop...
	 * |                        |
	 * +------------------------+   <- new frame start sp
	 * |                        |
	 * |     Local Variables    |   <- load store...
	 * |                        |
	 * +------------------------+   <- old frame sp
	 * |                        |
	 * |        Arguments       |   <- old frame will push n args to operand stack.
	 * |                        |
	 * +------------------------+   <- new frame bp, bp+n is nth local variable.
	 * |   Old Frame Operands   |
	 * |         ....           |
	 * +------------------------+
	 */
	protected int bp;
	
	/**
	 * The open upvalues are captured by other frames in the current
	 * frame.
	 */
	private Upvalue[] openUpvalues;
	
	private Frame(OperandStack opStack, PrototypeFunction prototypeFunc) {
		this.closure = prototypeFunc;
		this.pc = prototypeFunc.pc;
		this.bp = opStack.sp - prototypeFunc.arity();
	}
	
	public Upvalue[] captureUpvalueObjs(OperandStack opStack, ConstantValue.MethodInfo methodInfo) {
		final int COUNT = methodInfo.upvalueCount();
		if (COUNT == 0) {
			return Upvalue.EMPTY_UPVALUES;
		}
		Upvalue[] upvalueObjs = new Upvalue[COUNT];
		for (int i = 0; i < COUNT; i ++) {
			int index = methodInfo.upvalueIndex(i);
			// checks the variable is in this frame.
			if (methodInfo.isLocal(i)) {
				upvalueObjs[i] = captureUpvalue(opStack, index);
			} else {
				// derive directly the upvalue from this frame.
				// the upvalue is captured by this frame in upper frames.
				upvalueObjs[i] = this.closure.upvalues[index];
			}
		}
		return upvalueObjs;
	}
	
	/**
	 * Captures a local variable in this frame.
	 */
	private Upvalue captureUpvalue(OperandStack opStack, int index) {
		if (openUpvalues == null) { /* lazy init */
			openUpvalues = new Upvalue[getMaxLocal()];
		} else if (openUpvalues[index] != null) { // Find the same upvalue.
			return openUpvalues[index];
		}
		Upvalue openUpvalue = new Upvalue(opStack.operands, bp + index);
		openUpvalues[index] = openUpvalue;
		return openUpvalue;
	}
	
	protected void closeUpvalues(ConstantValue.CloseIndexes closeInfo) {
		if (openUpvalues == null) {
			return;
		}
		for (int i = 0; i < openUpvalues.length; i ++) {
			if (closeInfo.hasUpvalueIndex(i) && openUpvalues[i] != null) {
				openUpvalues[i].close();
				openUpvalues[i] = null;
			}
		}
	}
	
	protected void closeAllUpvalues() {
		if (openUpvalues == null) {
			return;
		}
		for (Upvalue upvalue : openUpvalues) {
			if (upvalue != null)
				upvalue.close();
		}
		openUpvalues = null;
	}
	
	/**
	 * Returns the name of this frame, usually a function name. 
	 * If this frame is a top frame of a source file, the name is a 
	 * source file name.
	 */
	public String getName() {
		return closure.funcName();
	}
	
	public String getSourceFileName() {
		return closure.chunk.getSourceFileName();
	}
	
	public FileEnvironment getFileEnv() {
		return closure.fileEnv;
	}
	
	public Chunk getChunk() {
		return closure.chunk;
	}
	
	public CodeAttribute getCodeAttr() {
		return closure.codeAttr;
	}
	
	public int getMaxLocal() {
		return closure.codeAttr.maxLocal;
	}
	
	public int getMaxStack() {
		return closure.codeAttr.maxStack;
	}
	
	public ErrorHandlerTable getErrorHandlerTable() {
		return closure.codeAttr.errorHandlerTable;
	}
	
	public int currentLine() {
		return closure.chunk.getLineNumber(pc);
	}
	
	/**
	 * Returns the current line number, assuming the instruction that
	 * want to locate has executed.
	 */
	public int currentLineExecuted() {
		return closure.chunk.getLineNumber(pc-1);
	}
	
	public int frameSize() {
		return getMaxLocal() + getMaxStack();
	}
	
	public int slotCount() {
		return getMaxLocal();
	}
	
	public int getBp() {
		return bp;
	}
}
