package com.nano.candy.interpreter.i2.tool.debug;

import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.interpreter.i2.InterpreterImpl;
import com.nano.candy.interpreter.i2.builtin.type.error.CompilerError;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.tool.Compiler;
import com.nano.candy.interpreter.i2.vm.CarrierErrorException;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.main.CandyOptions;
import com.nano.candy.tool.CandyTool;
import com.nano.candy.tool.ToolException;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Options;
import java.io.File;
import java.io.IOException;

public class DebugerTool implements CandyTool {

	@Override
	public String groupName() {
		return "Debugger";
	}

	@Override
	public String groupHelper() {
		return "Debugger is used to set breakpoints, "
			+ "print runtime information, view source file, etc.";
	}

	@Override
	public String[] aliases() {
		return new String[]{"debug"};
	}
	
	@Override
	public void defineOptions(Options options) {}

	@Override
	public void run(Interpreter interpreter, CandyOptions options) throws Exception {
		if (!(interpreter instanceof InterpreterImpl)) {
			throw new ToolException("Unsupported non-i2 interpreter.");
		}
		if (options.getFiles().length == 0) {
			throw new ToolException("Debugger requires at least one source file.");
		}
		options.getInterpreterOptions().setIsDebugMode(true);
		InterpreterImpl i2Interpreter = (InterpreterImpl) interpreter;
		Chunk chunk = compile(options.getFiles()[0], options.getInterpreterOptions());
		if (chunk == null) System.exit(1);
		run(i2Interpreter, chunk);
	}

	private Chunk compile(File file, InterpreterOptions options) {
		try {
			return Compiler.compileChunk(file, options, false);
		} catch (CarrierErrorException e) {
			if (e.getErrorObj() instanceof CompilerError) {	
				try {
					Logger.getLogger().printAllMessage(true);
				} catch (IOException el) {}
				return null;
			}
			throw e;
		}
	}

	private void run(InterpreterImpl i2Interpreter, Chunk chunk) {
		VM vm = i2Interpreter.getVM();
		VMTracer vmTracer = new VMTracer(vm);
		while (true) {
			i2Interpreter.initOrReset();
			vm.getTracerManager().registerCodeMonitor(vmTracer);
			vm.loadChunk(chunk);
			i2Interpreter.run();
			vmTracer.endCommand();
		}
	}

}
