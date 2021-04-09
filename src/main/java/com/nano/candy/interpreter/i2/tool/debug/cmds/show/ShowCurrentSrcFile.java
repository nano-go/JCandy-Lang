package com.nano.candy.interpreter.i2.tool.debug.cmds.show;

import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.VmMonitor;
import com.nano.common.io.FileUtilsQuiet;
import java.io.File;

public class ShowCurrentSrcFile extends AbstractCommand {

	@Override
	public String name() {
		return "file";
	}

	@Override
	public String description() {
		return "Show the source file in the current context.";
	}

	@Override
	public void startToExe(VmMonitor monitor, CommandLine cmdLine) throws CommandLine.ParserException {
		Chunk chunk = monitor.getVM().getFrameStack().peek().chunk;
		String name = chunk.getSourceFileName();
		String text = FileUtilsQuiet.readText(new File(name));
		monitor.getConsole().getPrinter().println(text);
	}
}
