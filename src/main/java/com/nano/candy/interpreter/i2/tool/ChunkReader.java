package com.nano.candy.interpreter.i2.tool;

import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantPool;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.LineNumberTable;

public class ChunkReader {
	
	public Chunk chunk;
	public int pc;
	
	private byte[] code;
	private ConstantValue[] cp;
	
	public ChunkReader(Chunk chunk) {
		this.chunk = chunk;
		this.code = chunk.getByteCode();
		this.cp = chunk.getConstantPool().getConstants();
	}
	
	public boolean readAtEnd() {
		return pc < code.length;
	}
	
	public byte readByte() {
		return code[pc ++];
	}

	public int readIndex() {
		if (code[pc] != (byte) 0xFF) {
			return code[pc ++] & 0xFF;
		}
		pc ++;
		return ((code[pc ++] << 8) & 0xFFFF) | code[pc ++] & 0xFF;
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
		return chunk.getSourceFileName();
	}
	
	public short getSlots() {
		return (short) chunk.getCodeAttr().maxLocal;
	}
	
	public boolean isLineNumberTablePresent() {
		return chunk.getLineNumberTable() != null;
	}
	
	public LineNumberTable getLineNumberTable(){
		return chunk.getLineNumberTable();
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
