package com.nano.candy.interpreter.i2.tool.debug;
import com.nano.candy.interpreter.i2.tool.debug.cmds.Help;
import com.nano.candy.interpreter.i2.tool.debug.cmds.Jump;
import com.nano.candy.interpreter.i2.tool.debug.cmds.Next;
import com.nano.candy.interpreter.i2.tool.debug.cmds.Quit;
import com.nano.candy.interpreter.i2.tool.debug.cmds.dis.DisassembleCmd;
import com.nano.candy.interpreter.i2.tool.debug.cmds.info.Information;
import com.nano.candy.interpreter.i2.tool.debug.cmds.show.Show;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class CommandManager {
	
	private HashSet<Command> listOfCmds;
	private HashMap<String, Command> commands;
	
	public CommandManager() {
		listOfCmds = new LinkedHashSet<>();
		commands = new HashMap<>();
		init();
	}

	private void init() {
		register(new Help());
		
		register(new Information());
		register(new Show());
		register(new DisassembleCmd());
		
		register(new Next());
		register(new Jump());
		register(new Quit());
	}
	
	public void register(Command command) {
		listOfCmds.add(command);
		commands.put(command.name(), command);
		String[] aliase = command.aliases();
		if (aliase == null) {
			return;
		}
		for (String alias : command.aliases()) {
			commands.put(alias, command);
		}
	}
	
	public HashSet<Command> getCommands() {
		return listOfCmds;
	}
	
	public Command getCommand(String name) {
		return commands.get(name);
	}
}
