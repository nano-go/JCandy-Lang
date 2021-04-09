package com.nano.candy.interpreter.i2.tool.debug;

public interface Command extends Comparable<Command> {
	public String name();
	public String[] aliases();
	public String description();
	public CommandOptions options();
	
	public void startToExe(VmMonitor monitor, CommandLine cmdLine) 
		throws CommandLine.ParserException;
	public void run(VmMonitor monitor);
}
