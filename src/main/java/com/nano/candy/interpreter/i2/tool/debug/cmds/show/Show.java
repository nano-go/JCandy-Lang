package com.nano.candy.interpreter.i2.tool.debug.cmds.show;

import com.nano.candy.interpreter.i2.tool.debug.Command;
import com.nano.candy.interpreter.i2.tool.debug.Group;
import java.util.Set;

public class Show extends Group {

	@Override
	public String name() {
		return "show";
	}

	@Override
	public String description() {
		return "Show some parts of a source file or other things about the debugger.";
	}
	
	@Override
	public void defindSubcommands(Set<Command> subcommands) {
		subcommands.add(new ShowLines());
		subcommands.add(new ShowCurrentSrcFile());
	}
}
