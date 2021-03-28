package com.nano.candy.interpreter.i2.rtda;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.PrototypeFunctionObj;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ChunkAttributes;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import java.util.LinkedList;
import java.util.ListIterator;

public final class Frame {
	
	public String name;
	public Chunk chunk;
	public int pc;
	
	public OperandStack opStack;
	
	/**
	 * Load/Store local variavles.
	 */
	public CandyObject[] slots;
	
	/**
	 * Captured upvalues from the upper frame.
	 */
	public UpvalueObj[] capturedUpvalues;
	
	/**
	 * The open upvalues are captured by other frame in current
	 * frame.
	 */
	public LinkedList<UpvalueObj> openUpvalues;
	
	public FileScope fileScope;
	
	public Frame(Chunk chunk, FileScope scope) {
		this.slots = new CandyObject[chunk.getAttrs().slots.slots];
		this.opStack = new DynamicOperandStack(8);
		this.chunk = chunk;
		this.pc = 0;
		this.capturedUpvalues = null;
		this.fileScope = scope;
		
		// the non-real file path may be a directory path.
		if (scope.compiledFileInfo.isRealFile()) {
			this.name = scope.compiledFileInfo.getSimpleName();
		} else {
			// if the simple name is used here, the stack info easyly mislead
			// users when an error occurs.
			this.name = chunk.getSourceFileName();
		}
	}
	
	public Frame(PrototypeFunctionObj prototypeFunc) {
		this.slots = new CandyObject[prototypeFunc.slots];
		this.opStack = new FixedOperandStack(prototypeFunc.stackSize);
		this.name = prototypeFunc.declredName();
		this.chunk = prototypeFunc.chunk;
		this.pc = prototypeFunc.pc;
		this.capturedUpvalues = prototypeFunc.upvalues;
		this.fileScope = prototypeFunc.fileScope;
	}
	
	public UpvalueObj[] makeUpvalueObjs(ConstantValue.MethodInfo methodInfo) {
		final int COUNT = methodInfo.upvalueCount();
		UpvalueObj[] upvalueObjs = new UpvalueObj[COUNT];
		for (int i = 0; i < COUNT; i ++) {
			int index = methodInfo.upvalueIndex(i);
			// checks the upvalue is in current frame.
			if (methodInfo.isLocal(i)) {
				// captures the upvalue from current frame.
				upvalueObjs[i] = captureUpvalue(index);
			} else {
				// gets the upvalue from upper frame.
				upvalueObjs[i] = this.capturedUpvalues[index];
			}
		}
		return upvalueObjs;
	}
	
	/**
	 * Try to find the same open upvalue.
	 */
	private UpvalueObj captureUpvalue(int index) {
		if (openUpvalues == null) { /* lazy init */
			openUpvalues = new LinkedList<>();
		}

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
	
	public int currentLine() {
		ChunkAttributes.LineNumberTable lineNumberTable =
			chunk.getAttrs().lineNumberTable;
		if (lineNumberTable == null) {
			return -1;
		}
		return lineNumberTable.findLineNumber(pc-1);
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
	
	public CandyObject pop() {
		return opStack.pop();
	}

	public CandyObject peek(int k) {
		return opStack.peek(k);
	}
	
	public void push(CandyObject operand) {
		opStack.push(operand);
	}
	
	public void release() {
		closeAllUpvalues();
		chunk = null;
		opStack = null;
		slots = null;
		name = null;
	}

}
