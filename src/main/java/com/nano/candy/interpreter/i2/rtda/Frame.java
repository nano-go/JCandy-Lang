package com.nano.candy.interpreter.i2.rtda;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.PrototypeFunctionObj;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ChunkAttributes;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
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
	
	public String name;
	public Chunk chunk;
	public int pc;
	
	public OperandStack opStack;
	public boolean exitMethodAtReturn;
	
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
	
	private Frame() {}
	
	private Frame init(Chunk chunk, FileScope scope) {
		adaptForSlots(chunk.getAttrs().slots.slots);
		this.opStack = new DynamicOperandStack(8);
		this.chunk = chunk;
		this.pc = 0;
		this.fileScope = scope;
		
		if (scope.compiledFileInfo.isRealFile()) {
			this.name = scope.compiledFileInfo.getSimpleName();
		} else {
			this.name = chunk.getSourceFileName();
		}
		return this;
	}
	
	private Frame init(PrototypeFunctionObj prototypeFunc) {
		adaptForSlots(prototypeFunc.slots);
		this.opStack = new FixedOperandStack(prototypeFunc.stackSize);
		this.name = prototypeFunc.declredName();
		this.chunk = prototypeFunc.chunk;
		this.pc = prototypeFunc.pc;
		this.capturedUpvalues = prototypeFunc.upvalues;
		this.fileScope = prototypeFunc.fileScope;
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
				// derive the upvalue from this frame.
				upvalueObjs[i] = captureUpvalue(index);
			} else {
				// derive the upvalue from the upper frame.
				upvalueObjs[i] = this.capturedUpvalues[index];
			}
		}
		return upvalueObjs;
	}
	
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
		this.pc = 0;
		this.exitMethodAtReturn = false;
	}
}
