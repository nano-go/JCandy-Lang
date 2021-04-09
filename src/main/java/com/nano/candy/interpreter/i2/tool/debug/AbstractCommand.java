package com.nano.candy.interpreter.i2.tool.debug;

public abstract class AbstractCommand implements Command {

	@Override
	public int compareTo(Command cmd) {
		return name().compareTo(cmd.name());
	}
	
	@Override
	public String[] aliases() { return null; }

	@Override
	public CommandOptions options() { return null; }

	@Override
	public void run(VmMonitor monitor) {}
}
