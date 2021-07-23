package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import com.nano.candy.interpreter.i2.runtime.FileEnvironment;
import com.nano.candy.interpreter.i2.runtime.Frame;
import com.nano.candy.interpreter.i2.runtime.OperandStack;
import com.nano.candy.interpreter.i2.runtime.StackFrame;
import com.nano.candy.interpreter.i2.runtime.Upvalue;
import com.nano.candy.interpreter.i2.runtime.chunk.Chunk;
import com.nano.candy.interpreter.i2.runtime.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.runtime.chunk.attrs.CodeAttribute;

public final class PrototypeFunction extends CallableObj {

	public Chunk chunk;
	public ConstantValue.MethodInfo metInfo;
	public int pc;
	
	public FileEnvironment fileEnv;
	public Upvalue[] upvalues;
	
	public PrototypeFunction(Chunk chunk, int pc, 
	                         Upvalue[] upvalues, 
	                         String fullName, 
	                         ConstantValue.MethodInfo methodInfo, 
	                         FileEnvironment fileEnv)
	{
		super(
			methodInfo.name, fullName, 
			new ParametersInfo(methodInfo)
		);
		this.chunk = chunk;
		this.pc = pc;
		this.upvalues = upvalues;
		this.metInfo = methodInfo;
		this.fileEnv = fileEnv;
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
	public void onCall(CNIEnv env, OperandStack opStack, StackFrame stack, int argc, int unpackFlags) {
		Frame newFrame = Frame.fetchFrame(this, argc, opStack);	
		stack.pushFrame(newFrame);
	}
	
	@Override
	protected String strTag() {
		return "function";
	}
}
