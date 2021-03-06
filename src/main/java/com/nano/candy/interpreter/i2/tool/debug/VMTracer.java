package com.nano.candy.interpreter.i2.tool.debug;

import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.StackFrame;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantPool;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.interpreter.i2.vm.tracer.CodeTracer;
import com.nano.candy.interpreter.i2.vm.tracer.StackTracer;
import com.nano.candy.utils.Console;
import java.util.Arrays;

public class VMTracer implements StackTracer, CodeTracer {

	private VM vm;
	private CommandManager cmdManager;
	private Command runningCommand;
	private Console console;

	public VMTracer(VM vm) {
		this.vm = vm;
		this.cmdManager = new CommandManager();
		this.console = new Console(System.in, System.out);
	}
	
	public VM getVM() {
		return vm;
	}
	
	public int getPc() {
		vm.syncPcToTopFrame();
		return vm.getFrameStack().peek().pc;
	}
	
	/**
	 * Returns the constant pool in the current context.
	 */
	public ConstantPool getConstantPool() {
		vm.syncPcToTopFrame();
		Chunk chunk = vm.getFrameStack().peek().chunk;
		return chunk.getConstantPool();
	}
	
	/**
	 * Returns the source file name in the current context.
	 */
	public String getSourceFile() {
		return vm.getFrameStack().peek().chunk.getSourceFileName();
	}
	
	public Console getConsole() {
		return this.console;
	}
	
	public CommandManager getCommandManager() {
		return this.cmdManager;
	}
	
	public boolean hasRunningCommand() {
		return runningCommand != null;
	}
	
	public void runCommand(Command command) {
		this.runningCommand = command;
	}
	
	public void endCommand() {
		this.runningCommand = null;
	}
	
	@Override
	public void beforeIns(VM vm, int pc) {
		if (hasRunningCommand()) {
			runningCommand.run(this);
			if (hasRunningCommand()) {
				return;
			}
		}
		while (true) {
			String cmd = console.waitForUserInput();
			if (cmd == null) {
				System.exit(0);
			}
			String[] args = cmd.trim().split("[\\n \\r\\t\\f]+");
			try {
				exeCmd(vm, args);
				if (runningCommand != null) {
					break;
				}
			} catch (CommandLine.ParserException e) {
				console.getPrinter().println(e.getMessage());
			}
		}
	}

	public void exeCmd(VM vm, String[] args) throws CommandLine.ParserException {
		String name = args[0];
		Command command = cmdManager.getCommand(name);
		if (command == null) {
			console.getPrinter().printf("Undefined command: %s\n", args[0]);
			return;
		}
		exeCmd(command, Arrays.copyOfRange(args, 1, args.length));
	}
	
	public void exeCmd(Command cmd, String[] args) throws CommandLine.ParserException {
		CommandOptions options = cmd.options();
		if (options == null) {
			options = new CommandOptions();
		}
		CommandLine cmdline = new CommandLine(cmd, options);
		cmdline.parse(args);
		cmd.startToExe(this, cmdline);
	}

	@Override
	public void afterIns(VM vm) {}

	@Override
	public void newFramePushed(VM vm, StackFrame stack) {}

	@Override
	public void oldFramePoped(VM vm, Frame oldFrame, StackFrame stack) {}

}
