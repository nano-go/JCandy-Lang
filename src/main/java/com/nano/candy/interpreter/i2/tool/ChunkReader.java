package com.nano.candy.interpreter.i2.tool;

import com.nano.candy.interpreter.i2.rtda.Chunk;
import com.nano.candy.interpreter.i2.rtda.ChunkAttributes;
import com.nano.candy.interpreter.i2.rtda.ConstantPool;
import com.nano.candy.interpreter.i2.rtda.ConstantValue;

public class ChunkReader {
	
	public Chunk chunk;
	public int pc;
	
	private byte[] code;
	private ConstantValue[] cp;
	private ChunkAttributes attrs;

	public ChunkReader(Chunk chunk) {
		this.chunk = chunk;
		this.code = chunk.getByteCode();
		this.cp = chunk.getConstantPool().getConstants();
		this.attrs = chunk.getAttrs();
	}
	
	public boolean readAtEnd() {
		return pc < code.length;
	}
	
	public byte readByte() {
		return code[pc ++];
	}

	public int readIndex() {
		return readUint8();
	}

	public int readUint8() {
		return Byte.toUnsignedInt(code[pc ++]);
	}
	
	public int readInt16() {
		short value = (short)(code[pc] << 8 | 
			Byte.toUnsignedInt(code[pc + 1]));
		pc += 2;
		return value;
	}
	
	public String getFileName() {
		return attrs.getSourceFileName();
	}
	
	public short getSlots() {
		return attrs.getSlots();
	}
	
	public boolean isLineNumberTablePresent() {
		return getLineNumberTable() != null;
	}
	
	public ChunkAttributes.LineNumberTable getLineNumberTable(){
		return attrs.lineNumberTable;
	}
	
	public ConstantValue readConstant() {
		return readConstant(readIndex());
	}

	public ConstantValue readConstant(int index) {
		return cp[index];
	}
	
	public ConstantPool getCp() {
		return chunk.getConstantPool();
	}
	
	public ConstantValue[] getConstants() {
		return cp;
	}
}
