package com.nano.candy.cmd;

import com.nano.candy.cmd.CandyOptions;
import com.nano.candy.cmd.CandyTool;
import com.nano.candy.code.Chunk;
import com.nano.candy.debug.dis.DefaultDisassDumper;
import com.nano.candy.debug.dis.DisassInsDumper;
import com.nano.candy.debug.dis.Disassembler;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.InterpreterOptions;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class DisassembleTool implements CandyTool {
	
	@Override
	public String groupName() {
		return "Disassembler";
	}

	@Override
	public String groupHelper() {
		return "Disassemble the specified source files.";
	}

	@Override
	public String[] aliases() {
		return new String[]{"dis"};
	}
	
	@Override
	public void defineOptions(Options options) {
		options.addOption("-c", false, "Disassemble the function code.")
			.addOption("-v", false, "Show the additional information.");
	}
	
	@Override
	public void run(Interpreter interpreter, CandyOptions options) throws Exception {
		options.checkHasSrcFile();
		boolean printAdditionalInfo = options.getCmd().hasOption("-v");
		boolean printFunctionCode = options.getCmd().hasOption("-c");
			
		DefaultDisassDumper dumper = new DefaultDisassDumper();
		
		dumper.setIsDisassCodeAttr(printAdditionalInfo);
		dumper.setIsDisassConstantPool(printAdditionalInfo);
		dumper.setIsDisassLineNumberTable(printAdditionalInfo);
		dumper.setIsDisassFunctions(printFunctionCode || printAdditionalInfo);
		
		printDiss(
			options.getInterpreterOptions(), dumper, 
			options.getSourceFile()
		);
	}

	private void printDiss(InterpreterOptions options, DisassInsDumper dumper, File file) {
		Disassembler disassember = new Disassembler();
		try {
			Optional<Chunk> chunk = CandyCompiler.compile(file, true);
			if (chunk.isPresent()) {
				disassember.setChunk(chunk.get());
				System.out.println(dumper.dump(disassember.disassChunk()));
			}
		} catch (IOException e) {
			throw new CmdToolException(e.getMessage());
		}
	}
}
