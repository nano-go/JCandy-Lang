package com.nano.candy.interpreter.i2.tool.debug.cmds.info;

import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.VmMonitor;
import com.nano.candy.interpreter.i2.tool.debug.cmds.CmdHelper;
import com.nano.candy.interpreter.i2.vm.VM;

public class InfoGlobal extends AbstractCommand {

	@Override
	public String name() {
		return "global";
	}

	@Override
	public String[] aliases() {
		return new String[]{"gl"};
	}

	@Override
	public String description() {
		return "Print all global varibles in the current context.";
	}

	@Override
	public void startToExe(VmMonitor monitor, CommandLine cmdLine) throws CommandLine.ParserException {
		VM vm = monitor.getVM();
		CmdHelper.printVariables(
			monitor.getConsole(), vm.getFrameStack().peek().fileScope.vars
		);
	}
}
