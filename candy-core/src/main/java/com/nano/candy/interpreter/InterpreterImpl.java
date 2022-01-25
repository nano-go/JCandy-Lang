package com.nano.candy.interpreter;

import com.nano.candy.code.Chunk;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.interpreter.runtime.CandyThread;
import com.nano.candy.interpreter.runtime.VMExitException;

public class InterpreterImpl implements Interpreter {
	
	private InterpreterOptions options;

	public InterpreterImpl(InterpreterOptions options) {
		this.options = options;
	}
	
	@Override
	public int execute(Chunk chunk) {
		CandyThread mainThread = new CandyThread(Thread.currentThread(), options);
		int code = 0;
		try {
			mainThread.run(chunk);
		} catch (VMExitException e) {
			code = e.code;
		}
		CandyThread.waitOtherThreadsEnd();
		return code;
	}
}
