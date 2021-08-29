package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.code.Chunk;
import com.nano.candy.code.CodeAttribute;
import com.nano.candy.code.ConstantValue;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.runtime.FileEnvironment;
import com.nano.candy.interpreter.runtime.Frame;
import com.nano.candy.interpreter.runtime.OperandStack;
import com.nano.candy.interpreter.runtime.StackFrame;
import com.nano.candy.interpreter.runtime.Upvalue;

public final class PrototypeFunction extends CallableObj {

	public final Chunk chunk;
	public final CodeAttribute codeAttr;
	public final int pc;
	
	public final FileEnvironment fileEnv;
	public final Upvalue[] upvalues;
	
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
		this.codeAttr = methodInfo.attrs;
		this.fileEnv = fileEnv;
		
		this.localSizeWithoutArgs = getMaxLocal() - arity();
	}
	
	public PrototypeFunction(Chunk chunk, FileEnvironment fileEnv) {
		super(chunk.getSimpleName(), new ParametersInfo(0, -1));
		this.chunk = chunk;
		this.pc = 0;
		this.upvalues = Upvalue.EMPTY_UPVALUES;
		this.codeAttr = chunk.getCodeAttr();
		this.fileEnv = fileEnv;
		
		this.localSizeWithoutArgs = getMaxLocal();
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
		return codeAttr.maxLocal;
	}
	
	public int getMaxStack() {
		return codeAttr.maxStack;
	}
	
	public int getCodeLength() {
		return codeAttr.length;
	}
	
	public CodeAttribute getCodeAttr() {
		return codeAttr;
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
