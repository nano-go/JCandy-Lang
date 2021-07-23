package com.nano.candy.interpreter.i2.tool.dis;

import com.nano.candy.interpreter.i2.runtime.chunk.Chunk;
import com.nano.candy.interpreter.i2.runtime.chunk.ConstantPool;
import com.nano.candy.interpreter.i2.runtime.chunk.ConstantValue;

public class DisassMethod implements DisassInstruction {
	
	private Chunk chunk;
	private int cpIndex;
	private ConstantValue.MethodInfo metInfo;
	private DisassCodeBlock body;

	public DisassMethod(Chunk chunk, int cpIndex, 
	                    ConstantValue.MethodInfo metInfo, DisassCodeBlock body) {
		this.chunk = chunk;
		this.cpIndex = cpIndex;
		this.metInfo = metInfo;
		this.body = body;
	}

	@Override
	public String accept(DisassInsDumper dumper) {
		return dumper.dump(this);
	}
	
	@Override
	public Chunk getChunk() {
		return chunk;
	}

	@Override
	public boolean isExpandable() {
		return true;
	}

	@Override
	public int length() {
		if (metInfo.classDefinedIn != null) {
			return metInfo.getLength();
		}
		return metInfo.getLength() + ConstantPool.indexLength(cpIndex) + 1;
	}

	@Override
	public int pc() {
		if (metInfo.classDefinedIn != null) {
			return -1;
		}
		return metInfo.getFromPC() - (ConstantPool.indexLength(cpIndex) + 1);
	}
	
	public int bodyLength() {
		return body.length();
	}
	
	public DisassInstruction getInstruction(int index) {
		return body.getIns(index);
	}
	
	public DisassCodeBlock getCodeBlock() {
		return body;
	}
	
	public ConstantValue.MethodInfo getMethodInfo() {
		return metInfo;
	}
	
	public String getMethodName() {
		return metInfo.name;
	}
}
