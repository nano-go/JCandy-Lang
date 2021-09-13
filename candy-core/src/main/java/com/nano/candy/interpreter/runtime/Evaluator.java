package com.nano.candy.interpreter.runtime;
import com.nano.candy.code.Chunk;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.ModuleObj;

public interface Evaluator {
	
	/**
	 * The differnece between the 'call' and the 'eval';
	 *
	 *    'call': Only push a frame to stack top if the fn is a prototype
	 *            function written by Candy programmers.
	 *
	 *    'eval': Run the frame of the fn and return the result.
	 */
	public void call(CallableObj fn, int argc, int unpackFlags);
	public CandyObject eval(CallableObj fn, int unpackFlags, 
	                        CandyObject... args);
	
	public void pushFrame(Frame frame);
	public Frame popFrame();
	public Frame[] getStack();
	
	public ModuleObj eval(Chunk chunk);
	public ModuleObj eval(CompiledFileInfo file);
	public void eval(Frame frame, boolean exitJavaMethodAtReturn);
}
