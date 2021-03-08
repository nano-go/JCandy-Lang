package com.nano.candy.interpreter.i2.rtda;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
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
	public CandyObject[] slots;
	
	/**
	 * Captured upvalues from other frame.
	 */
	public UpvalueObj[] capturedUpvalues;
	
	/**
	 * Captured upvalues in current frame.
	 */
	public LinkedList<UpvalueObj> openUpvalues;
	
	public Frame(Chunk chunk) {
		this.slots = new CandyObject[chunk.getAttrs().slots.slots];
		this.opStack = new DynamicOperandStack(8);
		this.name = chunk.getSourceFileName();
		this.chunk = chunk;
		this.pc = 0;
		this.capturedUpvalues = null;
	}
	
	public Frame(UpvalueObj[] upvalues, String name, Chunk chunk, 
	             int pc, int slots, int stackSize) {
		this.slots = new CandyObject[slots];
		this.opStack = new FixedOperandStack(stackSize);
		this.name = name;
		this.chunk = chunk;
		this.pc = pc;
		this.capturedUpvalues = upvalues;
	}
	
	public UpvalueObj[] makeUpvalueObjs(ConstantValue.MethodInfo methodInfo) {
		final int COUNT = methodInfo.upvalueCount();
		UpvalueObj[] upvalueObjs = new UpvalueObj[COUNT];
		for (int i = 0; i < COUNT; i ++) {
			int index = methodInfo.upvalueIndex(i);
			if (methodInfo.isLocal(i)) {
				// capture upvalue from the current frame.
				upvalueObjs[i] = captureUpvalue(index);
			} else {
				upvalueObjs[i] = this.capturedUpvalues[index];
			}
		}
		return upvalueObjs;
	}
	
	/**
	 * Try to find the same open upvalue.
	 */
	private UpvalueObj captureUpvalue(int index) {
		if (openUpvalues == null) {
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
	
	public UpvalueObj[] upvalues() {
		return this.capturedUpvalues;
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
