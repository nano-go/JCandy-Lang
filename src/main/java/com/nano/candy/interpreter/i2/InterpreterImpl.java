package com.nano.candy.interpreter.i2;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.comp.Checker;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.i2.codegen.CodeGenerator;
import com.nano.candy.interpreter.i2.error.CandyRuntimeError;
import com.nano.candy.interpreter.i2.error.NativeError;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.vm.InstructionBenchmarking;
import com.nano.candy.interpreter.i2.vm.VM;
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
		StringBuilder error = new StringBuilder();
		error.append(e.getClass().getSimpleName())
			.append(": ").append(e.getMessage()).append("\n");
		printStackInfo(error);
		System.out.print(error);
	}

	private void printStackInfo(StringBuilder stackInfo) {
		vm.syncPcToFrame();
		int max = vm.sp() - Math.min(vm.sp(), 24);
		for (int sp = vm.sp(); sp > max; sp --) {
			Frame frame = vm.getFrameAt(sp);
			String info = String.format(
				"    at %s (%s: line %d)\n",
				frame.name,
				frame.chunk.getSourceFileName(),
				// Aborts error when the last instruction is executed.
				frame.chunk.getLineNumber(frame.pc-1)
			);
			stackInfo.append(info);
		}
		if (max != 0) {
			stackInfo.append("    More Frames...\n");
		}
		vm.clearStackFrame();
	}

}
