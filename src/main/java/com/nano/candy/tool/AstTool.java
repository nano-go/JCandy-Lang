package com.nano.candy.tool;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.printer.AstPrinter;
import com.nano.candy.ast.printer.AstPrinters;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.main.CandyOptions;
import com.nano.candy.parser.ParserFactory;
import com.nano.candy.utils.CommandLine;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Options;
import java.io.File;

public class AstTool implements CandyTool {

	@Override
	public void defineOptions(Options options) {
		options.newGroup("Ast")
			.addOption("-f", true, 
				"Print AST in the specified format.\nFormats:" +
				"\n    json/Json");
	}
	
	@Override
	public void run(Interpreter interpreter, CandyOptions options) throws Exception {	
		CommandLine cmd = options.getCmd();
		int flag = parseFlag(cmd);
		options.checkSrc();
		AstPrinter printer = AstPrinters.newPrinter(flag);
		for (File src : options.getFiles()) {
			Program program = ParserFactory.newParser(src).parse();
			Logger.getLogger().printAllMessage(true);
			printer.print(System.out, program);
		}
	}

	private int parseFlag(CommandLine cmd) {
		if (cmd.hasOption("-f")) {
			String format = cmd.getOptionArg("-f");
			switch (format) {
				case "json": case "Json":
					return AstPrinters.PRINT_AST_IN_JSON_MASK;
				default:
					throw new Options.ParseException("Unknown ast format: " + format);
			}
		}
		return AstPrinters.PRINT_AST_IN_JSON_MASK;
	}
	
}
