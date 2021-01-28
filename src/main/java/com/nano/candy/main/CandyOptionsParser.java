package com.nano.candy.main;

import com.nano.candy.config.Config;
import com.nano.common.io.FilePathUtils;
import java.io.File;
import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CandyOptionsParser {

	public static CandyOptions parse(String... args) throws ParseException {
		CandyOptions candyOptions = new CandyOptions();
		Options options = defineOptions();
		parseOptions(candyOptions, options, args);
		checkOptions(candyOptions);
		return candyOptions;
	}

	private static Options defineOptions() {
		return new Options()
			.addOption(null, "ast-json", false, "Print the abstract syntax tree by json format.")
			.addOption("h", "help", false, "Print command line helper.");
	}
	
	private static CommandLine parseOptions(CandyOptions candyOptions, Options options, String[] args) throws ParseException {
		CommandLine cmdLine = new DefaultParser().parse(defineOptions(), args);
		candyOptions.options = options;
		if (cmdLine.hasOption("ast-json")) {
			candyOptions.printAstFlag |= CandyOptions.PRINT_AST_BY_JSON_MASK;
		}
		candyOptions.printHelper = cmdLine.hasOption("h");
		candyOptions.inputFiles = getInputFiles(cmdLine.getArgs());
		candyOptions.interactively = cmdLine.getOptions().length == 0 && cmdLine.getArgList().size() == 0;
		return cmdLine;
	}

	private static File[] getInputFiles(String... args) {
		ArrayList<File> inputFiles = new ArrayList<>();
		for (String path : args) {
			inputFiles.addAll(
				FilePathUtils.getFilesByBfsOrder(
					new File(path),
					(f) -> f.isFile() && f.getName().endsWith("." + Config.FILE_SUFFIX)
				)
			);
		}
		return inputFiles.toArray(new File[0]);
	}
	
	private static void checkOptions(CandyOptions candyOptions) throws ParseException {
		if (candyOptions.isPrintHelper()) {
			return;
		}
		if (!candyOptions.interactively && candyOptions.inputFiles.length == 0) {
			throw new ParseException("The candy source files not found!");
		}
	}
	
}
