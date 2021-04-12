package com.nano.candy.interpreter.i2.tool.debug.cmds.info;

import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.FrameStack;
import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.CommandOptions;
import com.nano.candy.interpreter.i2.tool.debug.VmMonitor;
import com.nano.candy.interpreter.i2.tool.debug.cmds.StandardStyle;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.utils.Console;
import com.nano.candy.utils.StyleCode;

public class InfoStack extends AbstractCommand {

	@Override
	public String name() {
		return "stack";
	}

	@Override
	public String[] aliases() {
		return new String[]{"stk"};
	}

	@Override
	public String description() {
		return "Print information about the current stack.";
	}

	@Override
	public CommandOptions options() {
		return new CommandOptions()
			.addOption(CommandOptions.OptionType.STRING,
				"FuncName", true, 
				"Print information about stack frames selected by function name.");
	}

	@Override
	public void startToExe(VmMonitor monitor, CommandLine cmdline) {
		VM vm = monitor.getVM();
		FrameStack fs = vm.getFrameStack();
		if (fs.frameCount() == 0) {
			monitor.getConsole().getPrinter().println("No stack frame.");
			return;
		}
		vm.syncPcToTopFrame();
		if (cmdline.hasOption("FuncName")) {
			String funcName = cmdline.getString("FuncName");
			printFrame(monitor.getConsole(), fs, funcName);
			return;
		}
		printAllFrames(monitor.getConsole(), fs);
	}

	private void printFrame(Console console, FrameStack fs, String funcName) {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (int sp = 0; sp < fs.frameCount(); sp ++) {
			Frame frame = fs.peek(sp);
			if (funcName.equals(frame.getName())) {
				if (i != 0) {
					builder.append("\n");
				}
				builder.append(String.format("#%d: ", i));
				printFrame(builder, frame);
				i ++;
			}
		}
		if (i == 0) {
			console.getPrinter().printf("The fream not found: %s\n", funcName);
			return;
		}
		console.getPrinter().print(builder.toString());
	}
	
	private void printAllFrames(Console console, FrameStack fs) {
		StringBuilder builder = new StringBuilder();
		for (int sp = fs.sp()-1;; sp --) {
			Frame frame = fs.getAt(sp);
			builder.append(String.format("#%d: ", fs.sp()-1-sp));
			printFrame(builder, frame);
			if (sp <= 0) {
				break;
			}
			builder.append("\n");
		}
		console.getPrinter().print(builder.toString());
	}

	public static void printFrame(StringBuilder builder, Frame frame) {
		String frameName = StyleCode.render(frame.getName(), 
			StyleCode.YELLOW, StyleCode.BOLD);
		String srcFileName = StandardStyle.namesOrNumber(
			frame.chunk.getSourceFileName()
		);
		int lineNum = frame.currentLine();
		builder.append(String.format(
			"%s \n    at %s:%d\n", frameName, srcFileName, lineNum
		));
		builder.append(String.format(
			"current stack size: %d, ", frame.opStack.size())
		);
		builder.append("slots: ")
			.append(frame.slots.length).append("\n");
	}
}
