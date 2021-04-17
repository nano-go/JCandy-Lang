package com.nano.candy.interpreter.i2.tool.debug.cmds.info;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.VMTracer;
import com.nano.candy.interpreter.i2.tool.debug.cmds.CmdHelper;
import com.nano.candy.interpreter.i2.tool.debug.cmds.StandardStyle;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.utils.Console;

public class InfoSlots extends AbstractCommand {

	@Override
	public String name() {
		return "slots";
	}

	@Override
	public String description() {
		return "Print all slots of the current frame.";
	}

	@Override
	public void startToExe(VMTracer tracer, CommandLine cmdLine) throws CommandLine.ParserException {
		VM vm = tracer.getVM();
		CandyObject[] slots = vm.getFrameStack().peek().slots;
		printSlots(tracer.getConsole(), slots);
	}

	private void printSlots(Console console, CandyObject[] slots) {
		String NULL = StandardStyle.namesOrNumber("null");
		for (int i = 0; i < slots.length; i ++) {
			if (slots[i] == null) {
				console.getPrinter().printf(
					"#%d: %s\n", i, NULL
				);
				continue;
			}
			console.getPrinter().printf("Index %d -- ", i);
			CmdHelper.printObject(console, slots[i]);
			console.getPrinter().println();
		}
	}
	
}
