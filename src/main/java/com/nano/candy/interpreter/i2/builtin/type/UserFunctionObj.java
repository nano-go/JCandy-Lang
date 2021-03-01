package com.nano.candy.interpreter.i2.builtin.type;

import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.rtda.Chunk;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.UpvalueObj;
import com.nano.candy.interpreter.i2.vm.VM;

public class UserFunctionObj extends CallableObj {

	private Chunk chunk;
	private int pc;
	
	private UpvalueObj[] upvalues;
	private int slots;
	private int stackSize;
	
	public UserFunctionObj(Chunk chunk, int pc, String declredName, String name, 
	                       UpvalueObj[] upvalues, int arity, int slots, int stackSize) {
		super(declredName, name, arity);
		this.chunk = chunk;
		this.pc = pc;
		this.upvalues = upvalues;
		this.slots = slots;
		this.stackSize = stackSize;
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
		Frame newFrame = new Frame(upvalues, declredName(), 
			chunk, pc, slots, stackSize);
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
