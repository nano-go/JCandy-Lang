package com.nano.candy.interpreter.i2.tool.debug.cmds.dis;

import com.nano.candy.interpreter.i2.tool.Disassembler;
import com.nano.candy.interpreter.i2.tool.debug.Command;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.Group;
import com.nano.candy.interpreter.i2.tool.debug.VmMonitor;
import java.util.Set;

public class DisassembleCmd extends Group {

	@Override
	public String name() {
		return "disassemble";
	}

	@Override
	public String[] aliases() {
		return new String[] {"dis"};
	}

	@Override
	public String description() {
		return "Disassemble the current file.";
	}

	@Override
	public void defindSubcommands(Set<Command> subcommands) {
		subcommands.add(new DiassembleCurInsCmd());
	}

	@Override
	public void startToExe(VmMonitor monitor, CommandLine cmdLine) throws CommandLine.ParserException {
		if (cmdLine.getArgs().length == 0) {
			Disassembler disr = new Disassembler();
			disr.setDisAttr(true);
			disr.setDisConstantPool(true);
			disr.loadChunk(monitor.getVM().frame().chunk);
			String disassembledChunk = disr.disassemble();
			monitor.getConsole().getPrinter().print(disassembledChunk);
			return;
		}
		super.startToExe(monitor, cmdLine);
	}
	
}
