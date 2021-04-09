package com.nano.candy.interpreter.i2.tool.debug.cmds;

import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.VmMonitor;

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
	public void startToExe(VmMonitor monitor, CommandLine cmdLine) throws CommandLine.ParserException {
		monitor.runCommand(this);
	}

	@Override
	public void run(VmMonitor monitor) {
		monitor.endCommand();
	}
}
