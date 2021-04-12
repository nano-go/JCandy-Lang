package com.nano.candy.interpreter.i2.tool;

import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.vm.CarrierErrorException;
import com.nano.candy.main.CandyOptions;
import com.nano.candy.tool.CandyTool;
import com.nano.candy.utils.BlockView;
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
		options.addOption("-da", false, "Disassemble attributes.")
			.addOption("-dcp", false, "Disassemble the constantpool.");
	}
	
	@Override
	public void run(Interpreter interpreter, CandyOptions options) throws Exception {
		options.checkSrc();
		BlockView blockView = new BlockView();
		Disassembler disassember = new Disassembler();
		disassember.setDisAttr(options.getCmd().hasOption("-da"));
		disassember.setDisConstantPool(options.getCmd().hasOption("-dcp"));
		for (File src : options.getFiles()) {
			Chunk chunk;
			try {
				chunk = Compiler.compileChunk(src, false, false);
			} catch (CarrierErrorException e) {
				continue;
			}
			disassember.loadChunk(chunk);
			blockView.addBlock(
				src.getName(),
				src.getName(),
				disassember.disassemble()
			);
		}
		System.out.println(blockView.toString());
	}
}
