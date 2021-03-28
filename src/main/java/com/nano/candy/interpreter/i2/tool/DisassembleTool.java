package com.nano.candy.interpreter.i2.tool;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.comp.Checker;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.i2.codegen.CodeGenerator;
import com.nano.candy.interpreter.i2.instruction.Instructions;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ChunkAttributes;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.main.CandyOptions;
import com.nano.candy.parser.ParserFactory;
import com.nano.candy.tool.CandyTool;
import com.nano.candy.utils.BlockView;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Options;
import java.io.File;

import static com.nano.candy.interpreter.i2.instruction.Instructions.*;

public class DisassembleTool implements CandyTool {
	
	public static final DisassembleTool DISASSEMBLE_TOOL = new DisassembleTool();

	@Override
	public void defineOptions(Options options) {
		options.newGroup("Disassemble");
	}
	
	@Override
	public void run(Interpreter interpreter, CandyOptions options) throws Exception {
		options.checkSrc();
		BlockView blockView = new BlockView();
		for (File src : options.getFiles()) {
			ASTreeNode node = ParserFactory.newParser(src).parse();
			Logger.getLogger().printAllMessage(true);
			Checker.check(node);
			Logger.getLogger().printAllMessage(true);
			loadChunk(new CodeGenerator(false).genCode(node));
			blockView.addBlock(
				src.getName(),
				src.getName(),
				disassemble()
			);
		}
		System.out.println(blockView.toString());
	}
	
	DisassembleInfoBuilder builder;
	ChunkReader reader;
	
	// This is used in formating string for conatant pool index.
	int maxDigitsOfCPIndex;
	
	private DisassembleTool() {}
	
	public DisassembleTool(Chunk chunk) {
		loadChunk(chunk);
	}
	
	public String disassemble() {
		this.builder = new DisassembleInfoBuilder();	
		disassembleAttributes();
		disassembleConstants();
		disassembleCode();		
		return builder.toString();
	}

	private void loadChunk(Chunk chunk) {
		this.reader = new ChunkReader(chunk);
		this.maxDigitsOfCPIndex = (int)(Math.log10(
			reader.getConstants().length) + 1);
	}

	private void disassembleAttributes() {
		builder.enterBlock("Attributes");
		if (reader.isLineNumberTablePresent()) {
			disassembleLineNumberTable(reader.getLineNumberTable());
		}
		disassembleFileName();
		disassembleSlots();
		builder.endBlock();
	}
	
	private void disassembleFileName() {
		builder.enterBlock("Source File Name");
		builder.appendIndent().appendf("%s\n", reader.getFileName());
		builder.endBlock();
	}

	private void disassembleSlots() {
		builder.enterBlock("Slots");
		builder.appendIndent().appendf("global slots: %d\n", reader.getSlots());
		builder.endBlock();
	}
	
	private void disassembleLineNumberTable(ChunkAttributes.LineNumberTable lineNumberTable) {
		builder.enterBlock("LineNumberTable");
		final int LEN = lineNumberTable.tableBytes.length;
		for (int i = 0; i < LEN; i += 4) {
			builder.appendIndent();
			int startPc = lineNumberTable.startPc(i);
			int line = lineNumberTable.lineNumber(i);
			builder.appendf("<pc: %d, line: %d>\n", startPc, line);
		}
		builder.endBlock();
	}

	private void disassembleConstants() {
		builder.enterBlock("Constant Pool");
		ConstantValue[] constants = reader.getConstants();
		for (int i = 0; i < constants.length; i ++) {
			builder.appendIndent();
			String indexFormat = "#%-" + maxDigitsOfCPIndex + "d = ";
			builder.appendf(indexFormat, i);
			
			builder.appendf("%-14s", constants[i].headName());
			builder.append(constants[i]);
			builder.append("\n");
		}
		builder.endBlock();
	}

	private void disassembleCode() {
		builder.enterBlock("Code");
		while (reader.readAtEnd()) {
			disassembleIns();
		}
		builder.endBlock();
	}
	
	private void disassembleIns() {
		byte opcode;
		switch ((opcode = reader.readByte())) {
			case OP_FUN:
				defFunctionInstruction();
				return;
			case OP_CLASS:
				defClassInstruction();
				return;
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
			case OP_IMPORT:
				simpleInstructionWithConst(opcode);
				break;
			case OP_INVOKE:
			case OP_SUPER_INVOKE:
				invokeInstruction(opcode);
				break;
			case OP_CALL_SLOT:
				callSlotInstruction(opcode);
				break;
			case OP_CALL_GLOBAL:
				callGlobalInstruction(opcode);
				break;
			case OP_LOAD:
			case OP_STORE:
			case OP_POP_STORE:
			case OP_LOAD_UPVALUE:
			case OP_STORE_UPVALUE:
			case OP_CLOSE_SLOT:
			case OP_CLOSE_UPVALUE:
			case OP_CALL:
			case OP_APPEND:
				simpleInstructionWithArg(opcode, 1, true);
				break;
			case OP_POP_JUMP_IF_FALSE:
			case OP_POP_JUMP_IF_TRUE:
			case OP_JUMP_IF_FALSE:
			case OP_JUMP_IF_TRUE:
			case OP_JUMP:
			case OP_LOOP:
				simpleJumpInstruction(opcode);
				break;
			default:
				simpleInstruction(opcode);
				break;
		}
		builder.append("\n");
	}

	private void appendFuncBody(ConstantValue.MethodInfo methodInfo, String tag) {
		builder.enterBlock(
			"%s %s(%d)", tag, methodInfo.name, methodInfo.arity
		);
		builder.appendIndent()
			.appendf("Slots: %d, Stack Size: %d\n",
				methodInfo.slots, methodInfo.stackSize);
		if (methodInfo.upvalueCount() != 0) {
			builder.enterBlock("Upvalues");
			final int C = methodInfo.upvalueCount();
			for (int i = 0; i < C; i ++) {
				builder.appendIndent().appendf("<isLocal: %s, index: %d>\n",
					String.valueOf(methodInfo.isLocal(i)),
					methodInfo.upvalueIndex(i)
				);
			}
			builder.endBlock();
		}
		int endPc = reader.pc + methodInfo.codeBytes;
		while (reader.pc != endPc) {
			disassembleIns();
		}
		builder.endBlock();
	}

	private void defFunctionInstruction() {
		ConstantValue.MethodInfo methodInfo = (ConstantValue.MethodInfo)
			reader.readConstant();
		appendFuncBody(methodInfo, "fun");
	}
	
	private void defClassInstruction() {
		ConstantValue.ClassInfo classInfo = (ConstantValue.ClassInfo) 
			reader.readConstant();
		String tag = "class " + classInfo.className;
		reader.readByte();
		builder.enterBlock(tag);
		if (classInfo.initializer.isPresent()) {
			appendFuncBody(classInfo.initializer.get(), "");
		}
		for (ConstantValue.MethodInfo methodInfo : classInfo.methods) {
			appendFuncBody(methodInfo, "method");
		}
		builder.endBlock();
	}
	
	private void invokeInstruction(byte opcode) {
		simpleInstruction(opcode);
		int arity = reader.readUint8();
		String attr = reader.readConstant().toString();
		builder.appendf("%s(%d)", attr, arity);
	}
	
	private void callSlotInstruction(byte opcode) {
		simpleInstruction(opcode);
		int arity = reader.readUint8();
		int slot = reader.readUint8();
		builder.appendf("slot %d (%d)", slot, arity);
	}
	
	private void callGlobalInstruction(byte opcode) {
		simpleInstruction(opcode);
		int arity = reader.readUint8();
		String global = reader.readConstant().toString();
		builder.appendf("global %s(%d)", global, arity);
	}

	private void simpleInstructionWithConst(byte opcode) {
		simpleInstruction(opcode);
		int index = reader.readIndex();
		String indexFormat = "#%-" + maxDigitsOfCPIndex + "d";
		builder.appendf(indexFormat, index);
		builder.append("  // ").append(reader.readConstant(index));
	}
	
	private void simpleInstructionWithArg(byte opcode, int length, boolean unsgined) {
		simpleInstruction(opcode);
		long index = 0;
		long lsh = (length - 1) * 8;
		for (int i = 0; i < length; i ++) {
			int bt = unsgined ? reader.readUint8() : reader.readByte();
			index |= bt << lsh;
			lsh -= 8;
			
		}
		builder.appendf("'%d", index);
	}
	
	private void simpleJumpInstruction(byte opcode) {
		simpleInstruction(opcode);
		builder.appendf("'%d", reader.readInt16());
	}

	private void simpleInstruction(byte opcode) {
		builder.appendIndent().appendf("%2d: ", reader.pc-1);
		builder.appendf("%-18s", Instructions.getName(opcode));
	}

}
