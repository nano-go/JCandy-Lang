package com.nano.candy.debug.dis;

import com.nano.candy.code.Chunk;
import com.nano.candy.code.CodeAttribute;
import com.nano.candy.code.ConstantPool;
import com.nano.candy.code.LineNumberTable;

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
