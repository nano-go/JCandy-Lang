package com.nano.candy.interpreter.i2.tool.debug.cmds;
import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.Command;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.CommandOptions;
import com.nano.candy.interpreter.i2.tool.debug.Group;
import com.nano.candy.interpreter.i2.tool.debug.VMTracer;

public class Help extends AbstractCommand {
	
	public static void printHelp(VMTracer tracer, Command command) {
		tracer.getConsole().getPrinter().print(printHelp(command));
	}
	
	public static String printHelp(Command command) {
		if (command instanceof Group) {
			return printGroupCmdList((Group) command);
		}
		CommandOptions options = command.options();
		StringBuilder builder = new StringBuilder();
		builder.append(command.description()).append("\n");
		if (options == null) {
			return builder.toString();
		}
		builder.append("\nUsage: \n");
		for (CommandOptions.Option option : options.getOptions()) {
			// name -- description [type: type]
			builder.append(StandardStyle.cmd(option.getName()));
			builder.append(" -- ").append(option.getDescription());
			builder.append(String.format(" [type: %s]\n", option.getType()));
		}
		return builder.toString();
	}

	private static String printGroupCmdList(Group group) {
		StringBuilder builder = new StringBuilder();
		builder.append(group.description())
			.append("\n\nList of ")
			.append(group.name())
			.append(" subcommands: \n\n");
		builder.append(sprintCmdList(group.getListOfSubcmds()));
		return builder.toString();
	}
	
	private static String sprintCmdList(Iterable<Command> list) {
		StringBuilder builder = new StringBuilder();
		for (Command cmd : list) {
			builder.append(StandardStyle.cmd(cmd.name()));
			builder.append(String.format(" -- %s\n", cmd.description()));
		}
		return builder.toString();
	}
	
	public static String sprintListOfCommands(VMTracer tracer) {
		return sprintCmdList(tracer.getCommandManager().getCommands());
	}
	
	@Override
	public String name() {
		return "help";
	}

	@Override
	public String description() {
		return "Print list of commands or the helper of the specified command.";
	}

	@Override
	public void startToExe(VMTracer tracer, CommandLine cmdLine) throws CommandLine.ParserException {
		String[] args = cmdLine.getArgs();
		Command cmd = CmdHelper.findCommand(tracer.getCommandManager(), args);
		if (cmd == null) {
			tracer.getConsole().getPrinter().print(
				sprintListOfCommands(tracer)
			);
			return;
		}	
		printHelp(tracer, cmd);
	}
}
