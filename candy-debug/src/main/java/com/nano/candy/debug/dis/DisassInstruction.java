package com.nano.candy.debug.dis;

import com.nano.candy.code.Chunk;

public interface DisassInstruction {
	public Chunk getChunk();
	public boolean isExpandable();
	public int pc();
	// include self.
	public int length();
	public String accept(DisassInsDumper dumper);
}
