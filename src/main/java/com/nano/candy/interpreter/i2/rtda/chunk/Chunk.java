package com.nano.candy.interpreter.i2.rtda.chunk;

import com.nano.candy.interpreter.i2.rtda.chunk.attrs.CodeAttribute;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.LineNumberTable;
import java.io.File;

public class Chunk {

	protected byte[] code;
	protected String sourceFileName;
	
	protected ConstantPool constantPool;
	protected LineNumberTable lineNumberTable;
	protected CodeAttribute codeAttr;
	
	private Chunk() {}
	
	public String getSourceFileName() {
		if (sourceFileName == null) {
			return "Unknown Source File";
		}
		return sourceFileName;
	}
	
	public String getSimpleName() {
		return new File(sourceFileName).getName();
	}
	
	public byte[] getByteCode() {
		return code;
	}
	
	public ConstantPool getConstantPool(){
		return constantPool;
	}
	
	public ConstantValue[] getConstants() {
		return constantPool.getConstants();
	}
	
	public LineNumberTable getLineNumberTable() {
		return lineNumberTable;
	}
	
	public int getLineNumber(int pc) {
		if (lineNumberTable == null) {
			return -1;
		}
		return lineNumberTable.findLineNumber(pc);
	}
	
	public ConstantValue.MethodInfo findMethodInfoByPC(int pc) {
		for (ConstantValue val : getConstants()){
			if (!(val instanceof ConstantValue.MethodInfo)) {
				continue;
			}
			ConstantValue.MethodInfo methodInfo = (ConstantValue.MethodInfo) val;
			int from = methodInfo.getFromPC();
			int to = from + methodInfo.getLength();
			if (pc >= from && pc < to) {
				return methodInfo;
			}
		}
		return null;
	}

	public CodeAttribute getCodeAttr() {
		return codeAttr;
	}
	
	public int getMaxStack() {
		return codeAttr.maxStack;
	}
	
	public int getMaxLocal() {
		return codeAttr.maxLocal;
	}
	
	public static class Builder {
		protected byte[] code;
		protected String sourceFileName;

		protected ConstantPool constantPool;
		protected LineNumberTable lineNumberTable;
		protected CodeAttribute codeAttr;
		
		;public Builder(String sourceFileName, byte[] code) {
			this.sourceFileName = sourceFileName;
			this.code = code;
			this.constantPool = new ConstantPool(new ConstantValue[0]);
		}
		
		public Builder setLineNumberTable(LineNumberTable lineNumberTable) {
			this.lineNumberTable = lineNumberTable;
			return this;
		}
		
		public Builder setCodeAttr(CodeAttribute codeAttr) {
			this.codeAttr = codeAttr;
			return this;
		}
		
		public Builder setConstantPool(ConstantPool cp) {
			this.constantPool = cp;
			return this;
		}
		
		public Builder setConstantPool(ConstantValue[] cvs) {
			this.constantPool = new ConstantPool(cvs);
			return this;
		}
		
		public Chunk build() {
			Chunk chunk = new Chunk();
			chunk.sourceFileName = sourceFileName;
			chunk.code = code;
			chunk.constantPool = constantPool;
			chunk.lineNumberTable = lineNumberTable;
			chunk.codeAttr = codeAttr;
			return chunk;
		}
	}
	
}
