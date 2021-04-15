package com.nano.candy.interpreter.i2.tool.dis;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantPool;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;

public class DisassClass implements DisassInstruction {
	
	private Chunk chunk;
	private int cpIndex;
	private ConstantValue.ClassInfo clazzInfo;
	private DisassMethod[] methods;

	public DisassClass(Chunk chunk, int cpIndex, DisassMethod[] methods) {
		this.chunk = chunk;
		this.cpIndex = cpIndex;
		this.clazzInfo = (ConstantValue.ClassInfo) 
			chunk.getConstants()[cpIndex];
		this.methods = methods;
	}

	public ConstantValue.ClassInfo getClazzInfo() {
		return clazzInfo;
	}
	
	public String getClassName() {
		return clazzInfo.className;
	}
	
	public String getSuperClassName() {
		return clazzInfo.className;
	}
	
	public boolean hasSuperClass() {
		return clazzInfo.hasSuperClass;
	}
	
	@Override
	public String accept(DisassInsDumper dumper) {
		return dumper.dump(this);
	}

	@Override
	public boolean isExpandable() {
		return true;
	}

	@Override
	public Chunk getChunk() {
		return chunk;
	}

	@Override
	public int length() {
		return clazzInfo.getLength() + ConstantPool.indexLength(cpIndex) + 2;
	}

	@Override
	public int pc() {
		return clazzInfo.fromPC;
	}
	
	public DisassMethod[] getMethods() {
		return methods;
	}
}
