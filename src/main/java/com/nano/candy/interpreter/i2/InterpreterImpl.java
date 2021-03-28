package com.nano.candy.interpreter.i2;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.comp.Checker;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.i2.codegen.CodeGenerator;
import com.nano.candy.interpreter.i2.error.CandyRuntimeError;
import com.nano.candy.interpreter.i2.error.NativeError;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.interpreter.i2.vm.debug.InstructionBenchmarking;
import com.nano.candy.utils.Logger;

public class InterpreterImpl implements Interpreter {

	private VM vm = new VM();
	
	@Override
	public void initOrReset() {
		vm.reset();
	}

	@Override
	public void onExit() {
		if (InstructionBenchmarking.DEBUG) {
			InstructionBenchmarking.getInstance().printResult();
		}
	}
	
	@Override
	public void load(ASTreeNode node, boolean isInteractionMode) {
		Checker.check(node);
		if (Logger.getLogger().hadErrors()) {
			return;
		}
		vm.loadChunk(new CodeGenerator(isInteractionMode).genCode(node));
	}

	@Override
	public boolean run(boolean isInteratively) {
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
