package com.nano.candy.interpreter.i2;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.tool.Compiler;
import com.nano.candy.interpreter.i2.vm.CarrierErrorException;
import com.nano.candy.interpreter.i2.vm.VM;

public class InterpreterImpl implements Interpreter {

	private static final VM vm = new VM();
	private InterpreterOptions options;
	
	public VM getVM() {
		return vm;
	}

	@Override
	public void enter(InterpreterOptions options) {
		this.options = options;
	}
	
	@Override
	public void initOrReset() {
		vm.reset(options);	
	}

	@Override
	public void load(String text) {
		Chunk chunk;
		try {
			chunk = Compiler.compileText(text, options);
		} catch (CarrierErrorException e) {
			return;
		}
		vm.loadChunk(chunk);
	}
	
	@Override
	public void load(ASTreeNode node) {
		Chunk chunk;
		try {
			chunk = Compiler.compileTree(node, options, false);
		} catch (CarrierErrorException e) {
			return;
		}
		vm.loadChunk(chunk);
	}

	@Override
	public boolean run() {
		int code = vm.runHandleError();
		return code == 0;
	}

}
