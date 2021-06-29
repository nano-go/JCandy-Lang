package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.rtda.FileScope;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.Upvalue;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.CodeAttribute;
import com.nano.candy.interpreter.i2.vm.VM;

public class PrototypeFunction extends CallableObj {

	public Chunk chunk;
	public ConstantValue.MethodInfo metInfo;
	public int pc;
	
	public FileScope fileScope;
	public Upvalue[] upvalues;
	
	public PrototypeFunction(Chunk chunk, int pc, 
	                         Upvalue[] upvalues, 
	                         String fullName, 
	                         ConstantValue.MethodInfo methodInfo, 
	                         FileScope fileScope)
	{
		super(
			methodInfo.name, fullName, 
			new ParametersInfo(methodInfo)
		);
		this.chunk = chunk;
		this.pc = pc;
		this.upvalues = upvalues;
		this.metInfo = methodInfo;
		this.fileScope = fileScope;
	}

	public Chunk getChunk() {
		return chunk;
	}

	public int getPc() {
		return pc;
	}
	
	public Upvalue[] upvalues() {
		return upvalues;
	}

	public int getMaxLocal() {
		return metInfo.attrs.maxLocal;
	}
	
	public int getMaxStack() {
		return metInfo.attrs.maxStack;
	}
	
	public int getCodeLength() {
		return metInfo.attrs.length;
	}
	
	public CodeAttribute getCodeAttr() {
		return metInfo.attrs;
	}

	@Override
	public boolean isBuiltin() {
		return false;
	}

	@Override
	protected void onCall(VM vm, int argc, int unpackingBits) {
		vm.runProtytypeFunction(this, argc);
	}
	
	@Override
	protected String strTag() {
		return "function";
	}
}
