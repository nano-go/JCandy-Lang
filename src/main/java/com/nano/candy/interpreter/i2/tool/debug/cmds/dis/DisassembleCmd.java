package com.nano.candy.interpreter.i2.tool.debug.cmds.dis;

import com.nano.candy.interpreter.i2.tool.debug.Command;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.Group;
import com.nano.candy.interpreter.i2.tool.debug.VmMonitor;
import com.nano.candy.interpreter.i2.tool.dis.DefaultDisassDumper;
import com.nano.candy.interpreter.i2.tool.dis.DisassChunk;
import com.nano.candy.interpreter.i2.tool.dis.DisassInsDumper;
import com.nano.candy.interpreter.i2.tool.dis.Disassembler;
import java.util.Set;

public class DisassembleCmd extends Group {

	private Disassembler disassembler;
	private DisassInsDumper dumper;
	
	public DisassembleCmd() {
		disassembler = new Disassembler();
		dumper = new DefaultDisassDumper()
			.setIsDisassCodeAttr(true)
			.setIsDisassConstantPool(true)
			.setIsDisassFunctions(true);
	}
	
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
			disassembler.setChunk(monitor.getVM().frame().chunk);
			DisassChunk disassembledChunk = disassembler.disassChunk();
			monitor.getConsole().getPrinter().print(dumper.dump(disassembledChunk));
			return;
		}
		super.startToExe(monitor, cmdLine);
	}
	
}
