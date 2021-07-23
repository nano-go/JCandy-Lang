package com.nano.candy.interpreter.i2.runtime;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.runtime.chunk.Chunk;

public interface Evaluator {
	
	public void call(CallableObj fn, int argc, int unpackFlags);
	public CandyObject eval(CallableObj fn, 
							int unpackFlags, 
							CandyObject... args);
	
	public ModuleObj eval(Chunk chunk);
	public ModuleObj eval(CompiledFileInfo file);
	public void eval(Frame frame, boolean exitJavaMethodAtReturn);
}
