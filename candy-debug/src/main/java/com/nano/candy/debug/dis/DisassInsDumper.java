package com.nano.candy.debug.dis;

public interface DisassInsDumper {

	public String dump(DisassChunk chunk);
	public String dump(DisassCodeBlock codeBlock);
	public String dump(DisassClass ins);
	public String dump(DisassSimpleInstruction ins);
	public String dump(DisassMethod ins);
}
