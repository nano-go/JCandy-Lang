package com.nano.candy.interpreter.i2.rtda;
import java.util.ArrayList;

public class Chunk {

	private byte[] code;
	private ConstantPool constantPool;
	private ChunkAttributes attrs;
	
	public Chunk(byte[] code, ConstantValue[] constants, ChunkAttributes attrs) {
		this.code = code;
		this.constantPool = new ConstantPool(constants);
		this.attrs = attrs;
	}

	public Chunk(byte[] code) {
		this.code = code;
	}
	
	public String getSourceFileName() {
		return attrs.getSourceFileName();
	}
	
	public int getLineNumber(int pc) {
		if (attrs.lineNumberTable == null) {
			return -1;
		}
		return attrs.lineNumberTable.findLineNumber(pc);
	}

	public ChunkAttributes getAttrs() {
		return attrs;
	}
	
	public byte[] getByteCode() {
		return code;
	}
	
	public ConstantPool getConstantPool(){
		return constantPool;
	}
}
