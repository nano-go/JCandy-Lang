package com.nano.candy.interpreter.i2.codegen;

import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ChunkAttributes;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import java.util.ArrayList;
import java.util.Arrays;

import static com.nano.candy.interpreter.i2.instruction.Instructions.*;
import static com.nano.candy.interpreter.i2.rtda.chunk.ChunkAttributes.*;
import static com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue.*;

public class ChunkBuilder {
	
	public static final boolean DEBUG = false;
	
	public static final byte[] LOAD_INSTRUCTIONS = {
		OP_LOAD0, OP_LOAD1, OP_LOAD2, OP_LOAD3, OP_LOAD4 
	};
	
	public static final byte[] STORE_INSTRUCTIONS = {
		OP_STORE0, OP_STORE1, OP_STORE2, OP_STORE3, OP_STORE4 
	};
	
	private static class LineNumberInfo {
		short startPc;
		short lineNumber;
		public LineNumberInfo(short startPc, short lineNumber) {
			this.startPc = startPc;
			this.lineNumber = lineNumber;
		}
	}
	
	protected static class State {
		public int stackSize;
		public int curStackSize;
		
		public State enclosing;

		public State(State enclosing) {
			this.enclosing = enclosing;
			this.stackSize = 1;
		}
	
		public void push(int s) {
			curStackSize += s;
			stackSize = Math.max(curStackSize, stackSize);
		}
		
		public void pop(int s) {
			curStackSize -= s;
			if (DEBUG && curStackSize < 0) {
				System.err.println("Bad stack size: " + curStackSize);
			}
		}
	}

	private ArrayList<LineNumberInfo> lineNumberTable;
	private short globalSlots;
	private String sourceFileName;
	
	protected ConstantPool constantPool;
	
	private byte[] code = new byte[64];
	
	/**
	 * Code Pointer.
	 */
	private int cp;
	
	private State state;

	public ChunkBuilder() {
		this.lineNumberTable = new ArrayList<>();
		this.constantPool = new ConstantPool();
		this.state = new State(null);
	}

	public void setGlobalSlots(int slots) {
		this.globalSlots = (short)slots;
	}
	
	public void setSourceFileName(String fileName) {
		this.sourceFileName = fileName;
	}
	
	protected void addLineNumber(int startPc, int lineNumber) {
		if (lineNumber < 0) {
			return;
		}
		if (!lineNumberTable.isEmpty()) {
			LineNumberInfo lastLineInfo = lineNumberTable.get(lineNumberTable.size()-1);
			if (lastLineInfo.lineNumber == lineNumber) {
				return;
			}
		}
		lineNumberTable.add(new LineNumberInfo((short) startPc, (short) lineNumber));
	}
	

	public int curCp() {
		return cp;
	}
	
	public void emit1(byte b) {
		if (cp >= code.length) {
			code = Arrays.copyOf(code, code.length*2);
		}
		code[cp ++] = b;
	}
	
	public void emit2(int data) {
		if (cp + 1 >= code.length) {
			code = Arrays.copyOf(code, code.length*2);
		}
		code[cp ++] = (byte) (data >> 8);
		code[cp ++] = (byte) data;
	}
	
	public State state() {
		return state;
	}
	
	public void newState() {
		this.state = new State(state);
	}
	
	public void closeState() {
		this.state = state.enclosing;
	}
	
	public void updateState(byte opcode) {
		if (DEBUG) {
			/*System.out.printf(
				"%s: %d-> ", Instructions.getName(opcode), state.curStackSize
			);*/
		}
		switch (opcode) {
			case OP_LOAD:
			case OP_LOAD0:
			case OP_LOAD1:
			case OP_LOAD2:
			case OP_LOAD3:
			case OP_LOAD4:
			case OP_FUN:
			case OP_LOAD_UPVALUE:
			case OP_ICONST:
			case OP_DCONST:
			case OP_SCONST:
			case OP_TRUE:
			case OP_FALSE:
			case OP_NULL:
			case OP_NEW_ARRAY:
			case OP_BUILT_TUPLE:
			case OP_GLOBAL_GET:
			case OP_CALL_SLOT:
			case OP_CALL_GLOBAL:
			case OP_DUP:
				state.push(1);
				break;
			case OP_DUP_2:
				state.push(2);
				break;
			case OP_POP_STORE:
			case OP_POP:
			case OP_ADD:
			case OP_SUB:
			case OP_MUL:
			case OP_DIV:
			case OP_MOD:
			case OP_GT:
			case OP_GTEQ:
			case OP_LT:
			case OP_LTEQ:
			case OP_EQ:
			case OP_NOTEQ:
			case OP_INSTANCE_OF:
			case OP_POP_JUMP_IF_FALSE:
			case OP_POP_JUMP_IF_TRUE:
			case OP_GLOBAL_DEFINE:
			case OP_ASSERT:
			case OP_PRINT:
			case OP_IMPORT:
			case OP_SET_ATTR:
			case OP_SUPER_GET:
			case OP_SUPER_INVOKE:	
			// This opcode is only appear in top frame(global scope).
			// case OP_PRINT:
				state.pop(1);
				break;
		}
		if (DEBUG) {
			/*System.out.printf(
				"%d\n", state.curStackSize
			);*/
		}
	}
	
	/**
	 * Emit an opcode.
	 *
	 * @param line the line number to which the opcode is mapped.
	 */
	public void emitop(byte opcode, int line) {
		addLineNumber(cp, line);
		updateState(opcode);
		emit1(opcode);
	}
	
	public void emitop(byte opcode) {
		updateState(opcode);
		emit1(opcode);
	}
	
	/**
	 * Emit an opcode with 1 byte argument.
	 *
	 * @param b the 1 byte argument.
	 * @param line the line number to which the opcode is mapped.
	 */
	public void emitopWithArg(byte opcode, int b, int line) {
		emitop(opcode, line);
		emit1((byte) b);
	}
	
	public void emitopWithArg(byte opcode, int b) {
		emit1(opcode);
		emit1((byte) b);
	}
	
	/**
	 * Emit a lable opcode to be back patching.
	 *
	 * @return the lable pc.
	 */	
	public int emitLabel(byte opcode, int line) {
		return emitLabel(opcode, 0xFFFF, line);
	}

	public int emitLabel(byte opcode, int offset, int lineNumber) {
		emitop(opcode, lineNumber);
		emit2(offset);
		return cp - 2;
	}
	
	/**
	 * Emit a loop opcode to jump back the specified begainning position.
	 */
	public void emitLoop(int begainningPos, int lineNumber) {
		emitLabel(OP_LOOP, cp - begainningPos + 1, lineNumber);
	}

	/**
	 * Place two bytes into code at the specified position.
	 */
	public void backpatch(int position) {
		int offset = cp - position;
		code[position]     = (byte) (offset >> 8);
		code[position + 1] = (byte) offset;
	}
	
	/**
	 * Emit a byte to store the stack-top operand to the specified slot.
	 */
	public void emitStore(int slot, int line) {
		if (slot >= 0 && slot < STORE_INSTRUCTIONS.length) {
			emitop(STORE_INSTRUCTIONS[slot], line);
		} else {
			emitopWithArg(OP_STORE, slot, line);
		}
	}
	
	/**
	 * Emit a byte to load the specified slot to stack-top.
	 */
	public void emitLoad(int slot, int line) {
		if (slot >= 0 && slot < LOAD_INSTRUCTIONS.length) {
			emitop(LOAD_INSTRUCTIONS[slot], line);
		} else {
			emitopWithArg(OP_LOAD, slot, line);
		}
	}
	
	/**
	 * Emit bytes to invoke the given attribute with the specified
	 * argument count.
	 */
	public void emitInvoke(String attr, int arity, int line) {
		emitop(OP_INVOKE, line);
		emit1((byte) arity);
		emitStringConstant(attr);
	}

	
	private void emitIndex(int index) {
		if (index < 255) {
			emit1((byte) index);
			return;
		}
		emit1((byte) 0xFF);
		emit2(index);
	}
	
	/**
	 * Add the given constant into the constant pool and emit
	 * the index of the constant into code.
	 */
	public void emitConstant(ConstantValue constVal) {
		emitIndex(constantPool.addConstantValue(constVal));
	}

	public void emitIntegerConstant(long constVal) {
		emitIndex(constantPool.addInteger(constVal));
	}
	public void emitDoubleConstant(double constVal) {
		emitIndex(constantPool.addDouble(constVal));
	}
	public void emitStringConstant(String constVal) {
		emitIndex(constantPool.addString(constVal));
	}
	
	private Slots buildSlots() {
		if (globalSlots == -1) {
			return null;
		}
		return new Slots(globalSlots);
	}

	private SourceFileName buildSourceFileName() {
		if (sourceFileName == null) {
			return null;
		}
		return new SourceFileName(sourceFileName);
	}
	
	private LineNumberTable buildLineNumberTable() {
		byte[] bytes = new byte[lineNumberTable.size()*4];
		int offset = 0;
		for (LineNumberInfo line : lineNumberTable) {
			bytes[offset]     = (byte) (line.startPc >> 8);
			bytes[offset + 1] = (byte) line.startPc;
			bytes[offset + 2] = (byte) (line.lineNumber >> 8);
			bytes[offset + 3] = (byte) line.lineNumber;
			offset += 4;
		}
		return new LineNumberTable(bytes);
	}

	public Chunk build() {
		ChunkAttributes attrs = new ChunkAttributes(
			buildSourceFileName(),
			buildLineNumberTable(),
			buildSlots()
		);
		return new Chunk(Arrays.copyOf(code, cp), 
			constantPool.toConstants(), attrs);
	}
}
