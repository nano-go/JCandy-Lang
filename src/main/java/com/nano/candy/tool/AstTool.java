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
	public String groupName() {
		return "Abstract-Tree-Printer";
	}
	
	@Override
	public String groupHelper() {
		return "Print the AST of the specified source files.";
	}

	@Override
	public String[] aliases() {
		return new String[] {"ast"};
	}
	
	@Override
	public void defineOptions(Options options) {
		options.addOption("-f", true, 
				"Print AST in the specified format.\nFormats:" +
				"\n    json/Json");
	}
	
	@Override
	public void run(Interpreter interpreter, CandyOptions options) throws Exception {	
		CommandLine cmd = options.getCmd();
		int flag = parseFlag(cmd);
		options.checkHasSrcFile();
		AstPrinter printer = AstPrinters.newPrinter(flag);
		File src = options.getSourceFile();
		Program program = ParserFactory.newParser(src).parse();
		Logger.getLogger().printAllMessage(true);
		printer.print(System.out, program);
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
