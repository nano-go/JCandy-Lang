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

	public final Chunk chunk;
	public final ConstantValue.MethodInfo metInfo;
	public final int pc;
	
	public final FileEnvironment fileEnv;
	public final Upvalue[] upvalues;
	
	/**
	 * See Frame.java
	 *
	 * Cached: frameSize = maxLocal() + maxStack() - arity;
	 */
	public final int frameSize;
	
	/**
	 * See Frame.java
	 *
	 * Cached: localSizeWithoutArgs = maxLocal() - arity;
	 */
	public final int localSizeWithoutArgs;
	
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
		
		this.frameSize = getMaxLocal() + getMaxStack() - arity();
		this.localSizeWithoutArgs = getMaxLocal() - arity();
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
		env.getEvaluator().push(Frame.fetchFrame(this, opStack));
	}
	
	@Override
	protected String strTag() {
		return "function";
	}
}
