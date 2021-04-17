package com.nano.candy.interpreter.i2.tool.debug.cmds;

import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.CommandOptions;
import com.nano.candy.interpreter.i2.tool.debug.VMTracer;

public class Jump extends AbstractCommand {

	private int toPc;
	private Chunk chunk;
	
	@Override
	public String name() {
		return "jump";
	}

	@Override
	public String description() {
		return "Jump to the specified pc.";
	}

	@Override
	public CommandOptions options() {
		return new CommandOptions()
			.addOption(
				CommandOptions.OptionType.INTEGER, "pc", false, "The specified pc."
			);
	}

	@Override
	public void startToExe(VMTracer tracer, CommandLine cmdLine) throws CommandLine.ParserException {
		this.toPc = cmdLine.getInteger("pc");
		this.chunk = tracer.getVM().getFrameStack().peek().chunk;
		tracer.runCommand(this);
	}

	@Override
	public void run(VMTracer tracer) {
		if (toPc == tracer.getPc() && chunk == 
		    tracer.getVM().getFrameStack().peek().chunk) {
			chunk = null;
			tracer.endCommand();
		}
	}
}
