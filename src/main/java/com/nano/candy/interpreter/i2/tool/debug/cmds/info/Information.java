package com.nano.candy.interpreter.i2.tool.debug.cmds.info;

import com.nano.candy.interpreter.i2.tool.debug.Command;
import com.nano.candy.interpreter.i2.tool.debug.Group;
import java.util.Set;

public class Information extends Group {

	@Override
	public String name() {
		return "info";
	}

	@Override
	public String[] aliases() {
		return null;
	}

	@Override
	public String description() {
		return "Info command-set used to watch the status of the current context.";
	}

	@Override
	public void defindSubcommands(Set<Command> subcommands) {
		subcommands.add(new InfoStack());
		subcommands.add(new InfoSlots());
		subcommands.add(new InfoGlobal());
		subcommands.add(new InfoOperandStack());
	}
	
}
