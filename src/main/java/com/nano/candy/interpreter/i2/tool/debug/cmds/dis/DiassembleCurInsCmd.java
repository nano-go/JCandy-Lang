package com.nano.candy.interpreter.i2.tool.debug.cmds.dis;

import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.VmMonitor;
import com.nano.candy.interpreter.i2.tool.dis.DisassChunk;
import com.nano.candy.interpreter.i2.tool.dis.DisassInstruction;
import com.nano.candy.interpreter.i2.tool.dis.Disassembler;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.utils.Console;

public class DiassembleCurInsCmd extends AbstractCommand {

	private HighlightDisassemblerDumper dumper;
	
	public DiassembleCurInsCmd() {
		dumper = new HighlightDisassemblerDumper();
		dumper.setRange(-6, 6);
	}
	
	@Override
	public String name() {
		return "cur-ins";
	}

	@Override
	public String[] aliases() {
		return new String[]{"ci"};
	}

	@Override
	public String description() {
		return "Diassemble the instruction pointed by the current pc.";
	}

	@Override
	public void startToExe(VmMonitor monitor, CommandLine cmdLine) throws CommandLine.ParserException {
		VM vm = monitor.getVM();
		vm.syncPcToTopFrame();
		Chunk chunk = vm.frame().chunk;
		Console console = monitor.getConsole();
		int pc = vm.frame().pc;
		
		dumper.setHighlightPc(pc);
		ConstantValue.MethodInfo methodInfo = chunk.findMethodInfoByPC(pc);
		if (methodInfo != null) {
			DisassInstruction disassIns =
				new Disassembler(chunk).disassMethod(methodInfo);
			console.getPrinter().println(disassIns.accept(dumper));
		} else {
			DisassChunk disassChunk =
				new Disassembler(chunk).disassChunk();
			console.getPrinter().print(dumper.dump(disassChunk));
		}
	}
	
}
