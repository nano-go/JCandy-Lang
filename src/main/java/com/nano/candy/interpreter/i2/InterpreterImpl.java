package com.nano.candy.interpreter.i2;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.interpreter.i2.runtime.CarrierErrorException;
import com.nano.candy.interpreter.i2.runtime.EvaluatorEnv;
import com.nano.candy.interpreter.i2.runtime.chunk.Chunk;
import com.nano.candy.interpreter.i2.tool.Compiler;

public class InterpreterImpl implements Interpreter {

	// private static final VM vm = new VM();
	private InterpreterOptions options;
	private Chunk loadedChunk;
	
	@Override
	public void enter(InterpreterOptions options) {
		this.options = options;
	}
	
	@Override
	public void initOrReset() {
		// vm.reset(options);	
	}

	@Override
	public void load(String text) {
		Chunk chunk;
		try {
			chunk = Compiler.compileText(text, options);
		} catch (CarrierErrorException e) {
			return;
		}
		this.loadedChunk = chunk;
	}
	
	@Override
	public void load(ASTreeNode node) {
		Chunk chunk;
		try {
			chunk = Compiler.compileTree(node, options, false);
		} catch (CarrierErrorException e) {
			return;
		}
		this.loadedChunk = chunk;
	}

	@Override
	public int run() {
		EvaluatorEnv env = new EvaluatorEnv(options);
		env.getEvaluator().eval(loadedChunk);
		return 0;
	}

}
