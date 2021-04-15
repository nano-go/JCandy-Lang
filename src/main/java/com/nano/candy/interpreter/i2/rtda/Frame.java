package com.nano.candy.interpreter.i2.rtda;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.PrototypeFunctionObj;
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
		new GenericObjectPool<Frame>(50, new Frame[0]);
	
	public static Frame fetchFrame(Chunk chunk, FileScope scope)  {
		Frame f = FRAME_POOL.fetch();
		if (f == null) {
			f = new Frame();
		}
		return f.init(chunk, scope);
	}
	
	public static Frame fetchFrame(PrototypeFunctionObj prototypeFunc) {
		Frame f = FRAME_POOL.fetch();
		if (f == null) {
			f = new Frame();
		}
		return f.init(prototypeFunc);
	}
	
	private String name;
	private CodeAttribute codeAttr;
	private boolean isTopFrame;
	
	/**
	 * see VM.runFrame(boolean)
	 */
	public boolean exitMethodAtReturn;
	
	public Chunk chunk;
	public int pc;
	public OperandStack opStack;
	public CandyObject[] slots;
	
	public FileScope fileScope;
	
	/**
	 * Captured upvalues from the upper frame.
	 */
	public UpvalueObj[] capturedUpvalues;
	
	/**
	 * The open upvalues are captured by other frame in the current
	 * frame.
	 */
	private LinkedList<UpvalueObj> openUpvalues;
	
	private Frame() {}
	
	private Frame init(Chunk chunk, FileScope scope) {
		adaptForSlots(chunk.getMaxLocal());
		this.opStack = new FixedOperandStack(chunk.getMaxStack());
		this.name = chunk.getSimpleName();
		this.chunk = chunk;
		this.codeAttr = chunk.getCodeAttr();
		this.pc = 0;
		this.fileScope = scope;
		this.isTopFrame = true;
		return this;
	}
	
	private Frame init(PrototypeFunctionObj prototypeFunc) {
		adaptForSlots(prototypeFunc.getMaxLocal());
		this.opStack = new FixedOperandStack(prototypeFunc.getMaxStack());
		this.name = prototypeFunc.declredName();
		this.chunk = prototypeFunc.chunk;
		this.codeAttr = prototypeFunc.metInfo.attrs;
		this.pc = prototypeFunc.pc;
		this.fileScope = prototypeFunc.fileScope;
		this.capturedUpvalues = prototypeFunc.upvalues;
		this.isTopFrame = false;
		return this;
	}
	
	private void adaptForSlots(int minCapacity) {
		if (this.slots == null || this.slots.length < minCapacity) {
			this.slots = new CandyObject[minCapacity];
		}
	}
	
	public UpvalueObj[] captureUpvalueObjs(ConstantValue.MethodInfo methodInfo) {
		final int COUNT = methodInfo.upvalueCount();
		UpvalueObj[] upvalueObjs = new UpvalueObj[COUNT];
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
	private UpvalueObj captureUpvalue(int index) {
		if (openUpvalues == null) { /* lazy init */
			openUpvalues = new LinkedList<>();
		}
		
		// Find the same upvalue.
		UpvalueObj openUpvalue = null;
		for (int i = openUpvalues.size()-1; i >= 0; i --) {
			openUpvalue = openUpvalues.get(i);
			if (index == openUpvalue.index()) {
				return openUpvalue;
			}
		}
		
		openUpvalue = new UpvalueObj(this.slots, index);
		openUpvalues.add(openUpvalue);
		return openUpvalue;
	}
	
	public void closeUpvalues(int index) {
		ListIterator<UpvalueObj> i = openUpvalues.listIterator();
		while (i.hasNext()) {
			UpvalueObj upvalue = i.next();
			if (upvalue.index() == index) {
				upvalue.close();
				i.remove();
				return;
			}
		}
		throw new Error();
	}
	
	public void closeAllUpvalues() {
		if (openUpvalues == null) {
			return;
		}
		ListIterator<UpvalueObj> i = openUpvalues.listIterator();
		while (i.hasNext()) {
			UpvalueObj upvalue = i.next();
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
	
	public boolean isTopFrame() {
		return isTopFrame;
	}
	
	public int currentLine() {
		return chunk.getLineNumber(pc);
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
	
	public void resetAllSlots() {
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
		this.resetAllSlots();
		this.chunk = null;
		this.name = null;
		this.opStack = null;
		this.capturedUpvalues = null;
		this.fileScope = null;
		this.codeAttr = null;	
		this.pc = 0;
		this.isTopFrame = false;
		this.exitMethodAtReturn = false;
	}
}
