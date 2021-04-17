package com.nano.candy.interpreter.i2.tool.debug.cmds;
import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.VMTracer;

public class Quit extends AbstractCommand {

	@Override
	public String name() {
		return "quit";
	}
	
	@Override
	public String description() {
		return "Quit debugger.";
	}

	@Override
	public void startToExe(VMTracer tracer, CommandLine cmdLine) throws CommandLine.ParserException {
		System.exit(0);
	}
}
