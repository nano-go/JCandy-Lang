package com.nano.candy.interpreter.i2.tool.dis;

import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantPool;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.CodeAttribute;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.LineNumberTable;

public class DisassChunk {
	private Chunk chunk;
	private DisassCodeBlock block;

	public DisassChunk(Chunk chunk, DisassCodeBlock block) {
		this.chunk = chunk;
		this.block = block;
	}
	
	public Chunk getChunk() {
		return chunk;
	}
	
	public DisassCodeBlock getCodeBlock() {
		return block;
	}
	
	public ConstantPool getConstsntPool() {
		return chunk.getConstantPool();
	}
	
	public LineNumberTable getLineNumberTable() {
		return chunk.getLineNumberTable();
	}
	
	public CodeAttribute getCodeAttribute() {
		return chunk.getCodeAttr();
	}
}
