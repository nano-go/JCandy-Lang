package com.nano.candy.interpreter.i2.tool.debug.cmds;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.tool.debug.Command;
import com.nano.candy.interpreter.i2.tool.debug.CommandManager;
import com.nano.candy.interpreter.i2.tool.debug.Group;
import com.nano.candy.utils.Console;
import com.nano.candy.utils.TableView;
import java.util.HashMap;
import java.util.Map;

public class CmdHelper {
	
	public static void printVariables(Console console, HashMap<String, CandyObject> vars) {
		if (vars.size() == 0) {
			return;
		}
		TableView tabView = new TableView();
		tabView.setSpace(" ".repeat(3));
		tabView.setHeaders("", "", "");
		for (Map.Entry<String, CandyObject> e : vars.entrySet()) {
			tabView.addItem(
				StandardStyle.namesOrNumber(e.getKey()), 
				e.getValue().getCandyClassName(),
				e.getValue().toString()
			);
		}
		// trim '\033'
		console.getPrinter().println("\033" + tabView.toString().trim());
	}
	
	public static void printObject(Console console, CandyObject obj) {
		console.getPrinter().printf("(%s -- %s)",
			StandardStyle.namesOrNumber(obj.getCandyClassName()), obj.toString()
		);
	}
	
	public static Command findCommand(CommandManager cmdmng, String[] args) {
		if (args.length == 0) {
			return null;
		}
		Command command = cmdmng.getCommand(args[0]);
		for (int i = 1; i < args.length; i ++) {
			if (command instanceof Group) {
				Command subcmd = ((Group)command).getSubcommand(args[i]);
				if (subcmd == null) {
					break;
				}
				command = subcmd;
			} else break;
		}
		return command;
	}
	
}
