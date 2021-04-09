package com.nano.candy.interpreter.i2.tool.debug.cmds.dis;

import com.nano.candy.interpreter.i2.instruction.Instructions;
import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.VmMonitor;
import com.nano.candy.interpreter.i2.tool.debug.cmds.StandardStyle;
import com.nano.candy.interpreter.i2.vm.VM;

public class DiassembleCurInsCmd extends AbstractCommand {

	@Override
	public String name() {
		return "cur-ins";
	}

	@Override
	public String[] aliases() {
		return new String[]{"ci"};
	}

	@Override
	public String description() {
		return "Diassemble the instruction pointed by the current pc.";
	}

	@Override
	public void startToExe(VmMonitor monitor, CommandLine cmdLine) throws CommandLine.ParserException {
		VM vm = monitor.getVM();
		vm.syncPcToTopFrame();
		int pc = vm.frame().pc;
		byte[] code = vm.frame().chunk.getByteCode();
		monitor.getConsole().getPrinter().println(
			StandardStyle.highlight(
				pc + ": " + Instructions.getName(code[pc])
			)
		);
	}
	
}
