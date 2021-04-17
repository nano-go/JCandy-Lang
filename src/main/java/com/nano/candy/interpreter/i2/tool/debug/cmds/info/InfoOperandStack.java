package com.nano.candy.interpreter.i2.tool.debug.cmds.info;
import com.nano.candy.interpreter.i2.rtda.OperandStack;
import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.VMTracer;
import com.nano.candy.interpreter.i2.tool.debug.cmds.CmdHelper;
import com.nano.candy.utils.Console;

public class InfoOperandStack extends AbstractCommand {
	
	@Override
	public String name() {
		return "operand-stack";
	}

	@Override
	public String[] aliases() {
		return new String[]{"os"};
	}

	@Override
	public String description() {
		return "Print the operand stack of the current frame.";
	}

	@Override
	public void startToExe(VMTracer tracer, CommandLine cmdLine) throws CommandLine.ParserException {
		OperandStack os = tracer.getVM().frame().opStack;
		Console console = tracer.getConsole();
		for (int i = 0; i < os.size(); i ++) {
			console.getPrinter().printf("#%d -> ", i);
			CmdHelper.printObject(console, os.peek(i));
			console.getPrinter().println();
		}
	}
}
