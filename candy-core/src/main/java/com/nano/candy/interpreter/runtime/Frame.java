package com.nano.candy.interpreter.runtime;

import com.nano.candy.code.Chunk;
import com.nano.candy.code.CodeAttribute;
import com.nano.candy.code.ConstantValue;
import com.nano.candy.code.ErrorHandlerTable;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.PrototypeFunction;

public final class Frame {
	
	public static Frame fetchFrame(OperandStack opStack, 
	                               Chunk chunk, 
								   FileEnvironment env)  {
		return new Frame().init(opStack, chunk, env);
	}
	
	public static Frame fetchFrame(PrototypeFunction prototypeFunc, 
								   OperandStack opStack) {	
		return new Frame().init(opStack, prototypeFunc);
	}
	
	/**
	 * The name of this frame, usually a function name. 
	 * If this frame is a top frame (non-function), the name is a 
	 * source file name.
	 */
	private String name;
	
	/**
	 * True if this frame is a top fream.
	 */
	private boolean isSourceFileFrame;
	
	/**
	 * True when the virtual machine executes the RETURN opcode, 
	 * the virtual machine returns from the eval method.
	 *
	 * This method is used to call Candy functions from Java.
	 */
	protected boolean exitRunAtReturn;
	
	protected int pc;
	protected Chunk chunk;
	protected CodeAttribute codeAttr;
	
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
	 * Local Variables Size.
	 */
	protected int localSizeWithoutArgs;
	
	protected OperandStack opStack;
	
	/**
	 * This file environment is recorded when a frame is created.
	 *
	 * The file environment is required that reference to the variables
	 * correctly when you call the frame in other modules, 
	 */
	protected FileEnvironment fileEnv;
	
	/**
	 * Captured upvalues from the upper frame.
	 */
	protected Upvalue[] capturedUpvalues;
	
	/**
	 * The open upvalues are captured by other frames in the current
	 * frame.
	 */
	private Upvalue[] openUpvalues;
	
	private Frame() {}
	
	private Frame init(OperandStack opStack, Chunk chunk, FileEnvironment env) {
		this.name = chunk.getSimpleName();
		this.chunk = chunk;
		this.codeAttr = chunk.getCodeAttr();
		this.pc = 0;
		this.fileEnv = env;
		
		this.bp = opStack.sp;
		this.localSizeWithoutArgs = getMaxLocal();
		this.opStack = opStack;
		return this;
	}
	
	private Frame init(OperandStack opStack, PrototypeFunction prototypeFunc) {
		this.name = prototypeFunc.funcName();
		this.chunk = prototypeFunc.chunk;
		this.codeAttr = prototypeFunc.metInfo.attrs;
		this.pc = prototypeFunc.pc;
		this.fileEnv = prototypeFunc.fileEnv;
		this.capturedUpvalues = prototypeFunc.upvalues;
		
		this.bp = opStack.sp - prototypeFunc.arity();
		this.localSizeWithoutArgs = prototypeFunc.localSizeWithoutArgs;
		this.opStack = opStack;
		return this;
	}
	
	public Upvalue[] captureUpvalueObjs(ConstantValue.MethodInfo methodInfo) {
		final int COUNT = methodInfo.upvalueCount();
		Upvalue[] upvalueObjs = new Upvalue[COUNT];
		for (int i = 0; i < COUNT; i ++) {
			int index = methodInfo.upvalueIndex(i);
			// checks the upvalue is in this frame.
			if (methodInfo.isLocal(i)) {		
				upvalueObjs[i] = captureUpvalue(index);
			} else {
				// derive the upvalue from the upper frame.
				upvalueObjs[i] = this.capturedUpvalues[index];
			}
		}
		return upvalueObjs;
	}
	
	/**
	 * Derive the upvalue from this frame.
	 */
	private Upvalue captureUpvalue(int index) {
		if (openUpvalues == null) { /* lazy init */
			openUpvalues = new Upvalue[getMaxLocal()];
		} else if (openUpvalues[index] != null) { // Find the same upvalue.
			return openUpvalues[index];
		}
		Upvalue openUpvalue = new Upvalue(this.opStack.operands, bp + index);
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
	
	public String getName() {
		return name;
	}
	
	public String getSourceFileName() {
		return chunk.getSourceFileName();
	}
	
	public CodeAttribute getCodeAttr() {
		return codeAttr;
	}
	
	public int getMaxLocal() {
		return codeAttr.maxLocal;
	}
	
	public int getMaxStack() {
		return codeAttr.maxStack;
	}
	
	public ErrorHandlerTable getErrorHandlerTable() {
		return codeAttr.errorHandlerTable;
	}
	
	public boolean isSourceFileFrame() {
		return isSourceFileFrame;
	}
	
	public int currentLine() {
		return chunk.getLineNumber(pc);
	}
	
	/**
	 * Returns the current line number, assuming the instruction that
	 * want to locate has executed.
	 */
	public int currentLineExecuted() {
		return chunk.getLineNumber(pc-1);
	}
	
	public int frameSize() {
		return getMaxLocal() + getMaxStack();
	}
	
	public OperandStack getOperandStack() {
		return opStack;
	}
	
	public int slotCount() {
		return getMaxLocal();
	}
	
	public CandyObject getSlotAt(int index) {
		if (index >= getMaxLocal()) {
			throw new IndexOutOfBoundsException(
				String.format("Index %d, Size: %d", index, slotCount()));
		}
		return opStack.operands[bp + index];
	}
}
