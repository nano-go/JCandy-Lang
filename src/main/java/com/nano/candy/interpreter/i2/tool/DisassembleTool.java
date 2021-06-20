package com.nano.candy.interpreter.i2.tool;

import com.nano.candy.cmd.CandyTool;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.tool.dis.DefaultDisassDumper;
import com.nano.candy.interpreter.i2.tool.dis.DisassInsDumper;
import com.nano.candy.interpreter.i2.tool.dis.Disassembler;
import com.nano.candy.interpreter.i2.vm.CarrierErrorException;
import com.nano.candy.main.CandyOptions;
import com.nano.candy.utils.Options;
import java.io.File;

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
			Chunk chunk = Compiler.compileChunk(file, options, false);
			disassember.setChunk(chunk);
			System.out.println(dumper.dump(disassember.disassChunk()));
		} catch (CarrierErrorException e) {
			System.exit(65);
			
		}
	}
}
