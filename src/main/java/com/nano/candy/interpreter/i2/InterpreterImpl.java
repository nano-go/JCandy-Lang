package com.nano.candy.interpreter.i2;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.i2.error.CandyRuntimeError;
import com.nano.candy.interpreter.i2.error.CompilerError;
import com.nano.candy.interpreter.i2.error.NativeError;
import com.nano.candy.interpreter.i2.rtda.FrameStack;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.moudle.CompiledFileInfo;
import com.nano.candy.interpreter.i2.tool.Compiler;
import com.nano.candy.interpreter.i2.vm.VM;

public class InterpreterImpl implements Interpreter {

	private static final VM vm = new VM();
	
	public VM getVM() {
		return vm;
	}
	
	@Override
	public void initOrReset() {
		vm.reset();	
	}

	@Override
	public void onExit() {}
	
	@Override
	public void load(ASTreeNode node, boolean isInteractionMode) {
		Chunk chunk;
		try {
			chunk = Compiler.compileTree(node, isInteractionMode, false, false);
		} catch (CompilerError e) {
			return;
		}
		loadChunk(chunk, isInteractionMode);
	}
	
	public void loadChunk(Chunk chunk, boolean isInteractionMode) {
		if (isInteractionMode) {
			vm.loadChunk(chunk);
		} else {
			vm.loadFile(new CompiledFileInfo(
				chunk.getSourceFileName(), chunk));
		}
	}

	@Override
	public boolean run() {
		try {
			vm.run();
		} catch (CandyRuntimeError e) {
			if (VM.DEBUG) {
				e.printStackTrace();
			}
			reportError(e);
			return false;
		} catch (Throwable t) {
			if (VM.DEBUG) {
				t.printStackTrace();
			}
			reportError(new NativeError(t));
			return false;
		}
		return true;
	}

	private void reportError(CandyRuntimeError e) {
		vm.syncPcToTopFrame();
		FrameStack stack = vm.getFrameStack();
		e.printStackTrace(stack, "    ", 24, System.out);
		stack.clearFrame();
	}

}
