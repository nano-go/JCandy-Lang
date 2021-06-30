package com.nano.candy.interpreter.i2.rtda;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.PrototypeFunction;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.CodeAttribute;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.ErrorHandlerTable;
import com.nano.candy.utils.objpool.GenericObjectPool;
import com.nano.candy.utils.objpool.Recyclable;

public final class Frame implements Recyclable {
	
	private static final GenericObjectPool<Frame> FRAME_POOL =
		new GenericObjectPool<Frame>(100, new Frame[0]);
	
	public static Frame fetchFrame(Chunk chunk, FileEnvironment env)  {
		Frame f = FRAME_POOL.fetch();
		if (f == null) {
			f = new Frame();
		}
		return f.init(chunk, env);
	}
	
	public static Frame fetchFrame(PrototypeFunction prototypeFunc) {
		Frame f = FRAME_POOL.fetch();
		if (f == null) {
			f = new Frame();
		}
		return f.init(prototypeFunc);
	}
	
	private String name;
	private CodeAttribute codeAttr;
	
	/**
	 * True if this fream is the top fream of a Candy source file.
	 */
	private boolean isSourceFileFrame;
	
	/**
	 * True if you want to end the VM.runFrame Java method at the end of
	 * this fream execution.
	 *
	 * The field can be used to call Candy methods in the Java language level.
	 */
	public boolean exitMethodAtReturn;
	
	/**
	 * The PC points to the current instruction of this frame.
	 */
	public int pc;
	
	public Chunk chunk;
	
	/**
	 * The operand stack holds the operand used by operators to
	 * perform operation.
	 *
	 * The operand stack is a fixed size that Candy compiler can compute
	 * the max depth of the operand stack.
	 */
	public OperandStack opStack;
	
	/**
	 * This slots used to hold the all local variables in this frame.
	 */
	public CandyObject[] slots;
	
	/**
	 * The file environment when the frame was created.
	 *
	 * The file environment is required for reference to the variables
	 * correctly when you call a method in other modules, 
	 */
	public FileEnvironment fileEnv;
	
	/**
	 * Captured upvalues from the upper frame.
	 */
	public Upvalue[] capturedUpvalues;
	
	/**
	 * The open upvalues are captured by other frame in the current
	 * frame.
	 */
	private Upvalue[] openUpvalues;
	
	private Frame() {}
	
	private Frame init(Chunk chunk, FileEnvironment env) {
		adaptForSlots(chunk.getMaxLocal());
		this.opStack = new OperandStack(chunk.getMaxStack());
		this.name = chunk.getSimpleName();
		this.chunk = chunk;
		this.codeAttr = chunk.getCodeAttr();
		this.pc = 0;
		this.fileEnv = env;
		this.isSourceFileFrame = true;
		return this;
	}
	
	private Frame init(PrototypeFunction prototypeFunc) {
		adaptForSlots(prototypeFunc.getMaxLocal());
		this.opStack = new OperandStack(prototypeFunc.getMaxStack());
		this.name = prototypeFunc.declaredName();
		this.chunk = prototypeFunc.chunk;
		this.codeAttr = prototypeFunc.metInfo.attrs;
		this.pc = prototypeFunc.pc;
		this.fileEnv = prototypeFunc.fileEnv;
		this.capturedUpvalues = prototypeFunc.upvalues;
		this.isSourceFileFrame = false;
		return this;
	}
	
	private void adaptForSlots(int minCapacity) {
		if (this.slots == null || this.slots.length < minCapacity) {
			this.slots = new CandyObject[minCapacity];
		}
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
			openUpvalues = new Upvalue[slots.length];
		} else if (openUpvalues[index] != null) { // Find the same upvalue.
			return openUpvalues[index];
		}	
		Upvalue openUpvalue = new Upvalue(this.slots, index);
		openUpvalues[index] = openUpvalue;
		return openUpvalue;
	}
	
	public void closeUpvalues(ConstantValue.CloseIndexes closeInfo) {
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
	
	public void closeAllUpvalues() {
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
	
	public ErrorHandlerTable getErrorHandlerTable() {
		return codeAttr.errorHandlerTable;
	}
	
	public int getMaxLocal() {
		return codeAttr.maxLocal;
	}
	
	public int getMaxStack() {
		return codeAttr.maxStack;
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
	
	public OperandStack getOperandStack() {
		return opStack;
	}
	
	public int slotCount() {
		return slots.length;
	}
	
	public CandyObject load(int slot) {
		return slots[slot];
	}
	
	public void store(int slot, CandyObject value) {
		slots[slot] = value;
	}
	
	public void clearSlots() {
		for (int i = 0; i < slots.length; i ++) {
			slots[i] = null;
		}
	}
	
	public CandyObject pop() {
		return opStack.pop();
	}

	public CandyObject peek(int k) {
		return opStack.peek(k);
	}
	
	public void push(CandyObject operand) {
		opStack.push(operand);
	}
	
	public void recycleSelf() {
		FRAME_POOL.recycle(this);
	}

	@Override
	public void release() {
		this.closeAllUpvalues();
		this.clearSlots();
		this.chunk = null;
		this.name = null;
		this.opStack = null;
		this.capturedUpvalues = null;
		this.fileEnv = null;
		this.codeAttr = null;	
		this.pc = 0;
		this.isSourceFileFrame = false;
		this.exitMethodAtReturn = false;
	}
}
