package com.nano.candy.cmd;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.dumper.AstDumper;
import com.nano.candy.ast.dumper.AstDumpers;
import com.nano.candy.ast.dumper.DumperOptions;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.parser.ParserFactory;
import com.nano.candy.utils.Logger;
import java.io.File;
import java.io.IOException;

public class AstTool implements CandyTool {

	@Override
	public String groupName() {
		return "Abstract-Tree-Printer";
	}

	@Override
	public String groupHelper() {
		return "Print the AST of the specified source file.";
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
		options.addOption("-p", false, "Print the position of AST nodes.");
	}

	@Override
	public void run(Interpreter interpreter, CandyOptions options) throws Exception {	
		CommandLine cmd = options.getCmd();
		AstDumper dumper = AstDumpers.newPrinter(parseFlag(cmd));
		DumperOptions dumperOptions = getDumperOptions(cmd);
		dumper.dump(dumperOptions, getAstTreeNode(options));
	}

	private ASTreeNode getAstTreeNode(CandyOptions options) throws IOException {
		options.checkHasSrcFile();
		File src = options.getSourceFile();
		Program program = ParserFactory.newParser(src).parse();
		Logger.getLogger().printAllMessage(true);
		return program;
	}

	private int parseFlag(CommandLine cmd) {
		if (cmd.hasOption("-f")) {
			String format = cmd.getOptionArg("-f");
			switch (format) {
				case "json": case "Json":
					return AstDumpers.JSON_MASK;
				default:
					throw new Options.ParseException("Unknown ast format: " + format);
			}
		}
		return AstDumpers.JSON_MASK;
	}
	
	private DumperOptions getDumperOptions(CommandLine cmd) {
		DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setIsDumpPosition(cmd.hasOption("-p"));
		return dumperOptions;
	}
}

