package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.rtda.FileScope;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.UpvalueObj;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.vm.VM;

public class PrototypeFunctionObj extends CallableObj {

	public Chunk chunk;
	public int pc;
	
	public FileScope fileScope;
	public UpvalueObj[] upvalues;
	public int slots;
	public int stackSize;
	
	public PrototypeFunctionObj(Chunk chunk, int pc, UpvalueObj[] upvalues, 
		                        String name, ConstantValue.MethodInfo methodInfo, 
								FileScope fileScope) {
		super(methodInfo.name, name, methodInfo.arity);
		this.chunk = chunk;
		this.pc = pc;
		this.upvalues = upvalues;
		this.slots = methodInfo.slots;
		this.stackSize = methodInfo.stackSize;
		this.fileScope = fileScope;
	}

	public Chunk getChunk() {
		return chunk;
	}

	public int getPc() {
		return pc;
	}
	
	public UpvalueObj[] upvalues() {
		return upvalues;
	}

	public int getSlots() {
		return slots;
	}

	@Override
	public boolean isBuiltin() {
		return false;
	}
	
	@Override
	public void onCall(VM vm) {
		Frame top = vm.frame();
		Frame newFrame = new Frame(this);
		for (int i = 0; i < arity; i ++) {
			newFrame.store(i, top.pop());
		}
		vm.pushFrame(newFrame);
	}

	@Override
	protected String toStringTag() {
		return "function";
	}
}
