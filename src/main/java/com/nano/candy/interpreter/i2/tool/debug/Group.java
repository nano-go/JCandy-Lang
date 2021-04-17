package com.nano.candy.interpreter.i2.tool.debug;
import com.nano.candy.interpreter.i2.tool.debug.cmds.Help;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public abstract class Group extends AbstractCommand {

	private Set<Command> listOfSubcmds;
	private HashMap<String, Command> subcommands;
	
	public Group() {
		listOfSubcmds = new TreeSet<>();
		defindSubcommands(listOfSubcmds);
		subcommands = new HashMap<String, Command>();
		for (Command command : listOfSubcmds) {
			subcommands.put(command.name(), command);
			String[] aliases = command.aliases();
			if (aliases == null) {
				continue;
			}
			for (String alias : aliases) 
				subcommands.put(alias, command);
		}
	}
	
	public Set<Command> getListOfSubcmds() {
		return listOfSubcmds;
	}

	public Command getSubcommand(String name) {
		return subcommands.get(name);
	}
	
	public abstract void defindSubcommands(Set<Command> subcommands);

	@Override
	public void startToExe(VMTracer tracer, CommandLine cmdLine) 
		throws CommandLine.ParserException {
		String[] args = cmdLine.getArgs();
		if (args.length == 0) {
			Help.printHelp(tracer, this);
			return;
		}
		Command subCommand = subcommands.get(args[0]);
		if (subCommand == null) {
			tracer.getConsole().getPrinter().printf(
				"The subcommand '%s' not found.\n\n", args[0]
			);
			Help.printHelp(tracer, this);
			return;
		}
		tracer.exeCmd(subCommand, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
	public void run(VMTracer monitor) {}
}
