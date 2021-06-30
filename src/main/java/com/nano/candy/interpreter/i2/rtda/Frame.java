package com.nano.candy.interpreter.i2.rtda;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.PrototypeFunction;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.CodeAttribute;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.ErrorHandlerTable;
import com.nano.candy.utils.objpool.GenericObjectPool;
import com.nano.candy.utils.objpool.Recyclable;
import java.util.LinkedList;
import java.util.ListIterator;

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
	private boolean isSourceFileFrame;
	
	/**
	 * see VM.runFrame(boolean)
	 */
	public boolean exitMethodAtReturn;
	
	public Chunk chunk;
	public int pc;
	public OperandStack opStack;
	public CandyObject[] slots;
	
	public FileEnvironment fileEnv;
	
	/**
	 * Captured upvalues from the upper frame.
	 */
	public Upvalue[] capturedUpvalues;
	
	/**
	 * The open upvalues are captured by other frame in the current
	 * frame.
	 */
	private LinkedList<Upvalue> openUpvalues;
	
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
			openUpvalues = new LinkedList<>();
		}
		
		// Find the same upvalue.
		Upvalue openUpvalue = null;
		for (int i = openUpvalues.size()-1; i >= 0; i --) {
			openUpvalue = openUpvalues.get(i);
			if (index == openUpvalue.index()) {
				return openUpvalue;
			}
		}
		
		openUpvalue = new Upvalue(this.slots, index);
		openUpvalues.add(openUpvalue);
		return openUpvalue;
	}
	
	public void closeUpvalues(ConstantValue.CloseIndexes closeInfo) {
		if (openUpvalues == null) {
			return;
		}
		ListIterator<Upvalue> i = openUpvalues.listIterator();
		while (i.hasNext()) {
			Upvalue upvalue = i.next();
			if (closeInfo.hasUpvalueIndex(upvalue.index())) {
				upvalue.close();
				i.remove();
			}
		}
	}
	
	public void closeAllUpvalues() {
		if (openUpvalues == null) {
			return;
		}
		ListIterator<Upvalue> i = openUpvalues.listIterator();
		while (i.hasNext()) {
			Upvalue upvalue = i.next();
			upvalue.close();
			i.remove();
		}
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
