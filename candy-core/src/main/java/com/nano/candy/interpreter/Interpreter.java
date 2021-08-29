package com.nano.candy.interpreter;
import com.nano.candy.code.Chunk;

public interface Interpreter {
	public int execute(Chunk chunk);
}
