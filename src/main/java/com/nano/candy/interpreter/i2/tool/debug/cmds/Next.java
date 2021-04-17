package com.nano.candy.interpreter.i2.tool.debug.cmds;

import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.VMTracer;

public class Next extends AbstractCommand {

	@Override
	public String name() {
		return "next";
	}

	@Override
	public String[] aliases() {
		return new String[]{"n"};
	}

	@Override
	public String description() {
		return "Execute a instruction.";
	}

	@Override
	public void startToExe(VMTracer tracer, CommandLine cmdLine) throws CommandLine.ParserException {
		tracer.runCommand(this);
	}

	@Override
	public void run(VMTracer tracer) {
		tracer.endCommand();
	}
}
