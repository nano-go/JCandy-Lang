package com.nano.candy.interpreter.i2.tool.dis;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantPool;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.CodeAttribute;
import java.util.ArrayList;

import static com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue.*;
import static com.nano.candy.interpreter.i2.instruction.Instructions.*;

public class Disassembler {
	
	private Chunk chunk;
	private byte[] code;
	
	public Disassembler() {}
	
	public Disassembler(Chunk chunk) {
		setChunk(chunk);
	}
	
	public void setChunk(Chunk chunk) {
		this.chunk = chunk;
		this.code = chunk.getByteCode();
	}
	
	private int readCpIndex(int pc) {
		if (code[pc] != (byte) 0xFF) {
			return code[pc] & 0xFF;
		}
		pc ++;
		return ((code[pc ++] << 8) & 0xFFFF) | code[pc ++] & 0xFF;
	}
	
	private int readJumpIndex(int pc) {
		return (code[pc] << 8) & 0xFFFF | code[pc + 1] & 0xFF;
	}
	
	private ConstantValue getConstantValue(int index) {
		return chunk.getConstantPool().getConstants()[index];
	}
	
	public DisassChunk disassChunk() {
		return new DisassChunk(chunk, disassCodeBlock(
			chunk.getCodeAttr(), 0, chunk.getByteCode().length
		));
	}
	
	public DisassInstruction disassInstruction(int pc) {
		byte[] code = chunk.getByteCode();
		byte opcode = code[pc];
		switch (opcode) {	
			case OP_FUN:
				return disassMethod(pc);
			case OP_CLASS:
				return disassClass(pc);
			case OP_ICONST:
			case OP_DCONST:	
			case OP_SCONST:
			case OP_GLOBAL_DEFINE:
			case OP_GLOBAL_SET:
			case OP_GLOBAL_GET:
			case OP_SET_ATTR:
			case OP_GET_ATTR:
			case OP_SUPER_GET:
			case OP_NEW_ARRAY:
			case OP_NEW_MAP:
			case OP_IMPORT:
			case OP_CLOSE:
				return disassSimpleInsWithConst(pc);
				
			case OP_INVOKE:
			case OP_SUPER_INVOKE:
				return disassInvokeInstruction(pc);
				
			case OP_CALL_SLOT:
				return disassCallSlotIns(pc);
			case OP_CALL_GLOBAL:
				return disassCallGlobalIns(pc);
			case OP_CALL_EX:
				return disassCallEx(pc);
			case OP_LOAD:
			case OP_STORE:
			case OP_POP_STORE:
			case OP_LOAD_UPVALUE:
			case OP_STORE_UPVALUE:
			case OP_CALL:
			case OP_BUILT_TUPLE:
			case OP_APPEND:
			case OP_PUT:
				return disassSimpleInsWithArg(pc, 1, true);
			case OP_POP_JUMP_IF_FALSE:
			case OP_POP_JUMP_IF_TRUE:
			case OP_JUMP_IF_FALSE:
			case OP_JUMP_IF_TRUE:
			case OP_JUMP:
			case OP_LOOP:
				return disassJumpIns(pc);
			case OP_MATCH_ERRORS:
				return disassMacthErrorsIns(pc);
			default:
				return disassSimpleInstruction(pc);
		}
	}
	
	public DisassMethod disassMethod(ConstantValue.MethodInfo metInfo) {
		return disassMethod(-1, metInfo);
	}
	
	private DisassMethod disassMethod(int pc) {
		int index = readCpIndex(++ pc);
		ConstantValue.MethodInfo metInfo = 
			(ConstantValue.MethodInfo) chunk.getConstants()[index];
		return disassMethod(index, metInfo);
	}

	private DisassMethod disassMethod(int index, ConstantValue.MethodInfo metInfo) {
		DisassCodeBlock codeBlock = disassCodeBlock(
			metInfo.attrs, metInfo.getFromPC(), metInfo.getFromPC() + metInfo.getLength()
		);
		return new DisassMethod(chunk, index, metInfo, codeBlock);
	}
	
	public DisassClass disassClass(ConstantValue.ClassInfo clazzInfo) {
		return disassClass(-1, clazzInfo);
	}
	
	private DisassClass disassClass(int pc) {
		int index = readCpIndex(++ pc);
		ConstantValue.ClassInfo clazzInfo = (ConstantValue.ClassInfo) 
			chunk.getConstants()[index];
		return disassClass(index, clazzInfo);
	}

	private DisassClass disassClass(int index, ConstantValue.ClassInfo clazzInfo) {
		ArrayList<DisassMethod> disMets = new ArrayList<>();
		if (clazzInfo.initializer.isPresent()) {
			disMets.add(disassMethod(clazzInfo.initializer.get()));
		}
		for (MethodInfo metInfo : clazzInfo.methods) {
			disMets.add(disassMethod(metInfo));
		}
		return new DisassClass(
			chunk, index, disMets.toArray(new DisassMethod[0])
		);
	}
	
	private DisassCodeBlock disassCodeBlock(CodeAttribute codeAttr, int fromPc, int toPc) {
		ArrayList<DisassInstruction> insSet = new ArrayList<>();
		int pc = fromPc;
		while (pc < toPc) {
			DisassInstruction dis = disassInstruction(pc);
			insSet.add(dis);
			pc += dis.length();
		}
		return new DisassCodeBlock(
			codeAttr, insSet.toArray(new DisassInstruction[0])
		);
	}

	private DisassSimpleInstruction disassSimpleInsWithConst(int pc) {
		int insPc = pc ++;
		int index = readCpIndex(pc);
		String argStr = getConstantValue(index).toString();
		return new DisassSimpleInstruction(
			chunk, insPc, ConstantPool.indexLength(index), argStr
		);
	}

	private DisassInstruction disassInvokeInstruction(int pc) {
		int insPc = pc ++;
		byte[] code = chunk.getByteCode();
		int arity = code[pc ++] & 0xFF; /* u2 */
		int index = readCpIndex(pc); /* u1 - u3 */
		String attr = getConstantValue(index).toString();
		String argStr = String.format("%s(%d)", attr, arity);
		return new DisassSimpleInstruction(
			chunk, insPc, ConstantPool.indexLength(index) + 1, argStr
		);
	}
	

	private DisassInstruction disassCallSlotIns(int pc) {
		int insPc = pc ++;
		int arity = code[pc ++] & 0xFF; /* u1 */
		int slot = code[pc] & 0xFF; /* u1 */
		String argStr = String.format("slot %d (%d)", slot, arity);
		return new DisassSimpleInstruction(
			chunk, insPc, 2, argStr
		);
	}

	private DisassInstruction disassCallGlobalIns(int pc) {
		int insPc = pc ++;
		int arity = code[pc ++] & 0xFF; /* u1 */
		int constIndex = readCpIndex(pc); /* u1 - u3 */
		String global = getConstantValue(constIndex).toString();
		String argStr = String.format("global %s(%d)", global, arity);
		return new DisassSimpleInstruction(
			chunk, insPc, 2, argStr
		);
	}
	
	private DisassInstruction disassCallEx(int pc) {
		int insPc = pc ++;
		int arity = code[pc ++] & 0xFF; /* u1 */
		int constIndex = readCpIndex(pc); /* u1 - u3 */
		String unpackFlags = getConstantValue(constIndex).toString();
		String argStr = String.format
			("arity: %d, unpackFlags: %s", arity, unpackFlags);
		return new DisassSimpleInstruction(
			chunk, insPc, 2, argStr
		);
	}

	private DisassInstruction disassSimpleInsWithArg(int pc, int length, boolean unsigned) {
		int insPc = pc ++;
		long number = 0;
		long lsh = (length - 1) * 8;
		for (int i = 0; i < length; i ++) {
			byte arg = code[pc ++];
			int bt = unsigned ? arg & 0xFF : arg;
			number |= bt << lsh;
			lsh -= 8;
		}
		String argStr = String.format("'%d", number);
		return new DisassSimpleInstruction(
			chunk, insPc, length, argStr
		);
	}

	private DisassInstruction disassJumpIns(int pc) {
		int insPc = pc ++;
		int offset = readJumpIndex(pc); /* u2 */
		String argStr = String.format("offset %d", offset);
		return new DisassSimpleInstruction(
			chunk, insPc, 2, argStr
		);
	}

	private DisassInstruction disassMacthErrorsIns(int pc) {
		int insPc = pc ++;
		int offset = readJumpIndex(pc); /* u2 */
		int errorCount = code[pc + 2] & 0xFF; /* u1 */
		String argStr = String.format("offset: %d, errors: %d", offset, errorCount);
		return new DisassSimpleInstruction(
			chunk, insPc, 3, argStr
		);
	}

	private DisassInstruction disassSimpleInstruction(int pc) {
		return new DisassSimpleInstruction(chunk, pc, 0, "");
	}
	
}

