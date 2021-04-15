package com.nano.candy.interpreter.i2.tool.dis;

import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;

public interface DisassInstruction {
	public Chunk getChunk();
	public boolean isExpandable();
	public int pc();
	// include self.
	public int length();
	public String accept(DisassInsDumper dumper);
}
