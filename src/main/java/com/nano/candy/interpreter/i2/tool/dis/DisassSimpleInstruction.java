package com.nano.candy.interpreter.i2.tool.dis;

import com.nano.candy.interpreter.i2.code.OpCodes;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;

public class DisassSimpleInstruction implements DisassInstruction {
	
	private Chunk chunk;
	private int pc;
	private int length;
	private String argumentInfo;

	public DisassSimpleInstruction(Chunk chunk, int pc, int length, String argumentInfo) {
		this.chunk = chunk;
		this.pc = pc;
		this.length = 1 + length;
		this.argumentInfo = argumentInfo;
	}
	
	@Override
	public String accept(DisassInsDumper dumper) {
		return dumper.dump(this);
	}
	
	@Override
	public boolean isExpandable() {
		return false;
	}
	
	@Override
	public Chunk getChunk() {
		return chunk;
	}
	
	@Override
	public int length() {
		return length;
	}
	
	@Override
	public int pc() {
		return pc;
	}
	
	public String getArgumentInfo() {
		return argumentInfo;
	}
	
	public byte getInsByte() {
		return chunk.getByteCode()[pc];
	}
	
	public String getInsName() {
		return OpCodes.getName(getInsByte());
	}
}
