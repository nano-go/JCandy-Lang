package com.nano.candy.main;

import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.tool.CandyTool;
import com.nano.candy.tool.CandyToolFactory;
import com.nano.candy.utils.CandyFileFilter;
import com.nano.candy.utils.CommandLine;
import com.nano.candy.utils.Options;
import com.nano.common.io.FilePathUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class CandyOptionsParser {

	public static CandyOptions parse(String... args) {
		CandyOptions candyOptions = new CandyOptions();
		Options options = defineOptions(new Options());
		parseOptions(candyOptions, options, args);
		return candyOptions;
	}

	private static Options defineOptions(Options options) {
		return options
			.addOption("-h", false, "Print command line helper.").build();
	}
	
	private static CommandLine parseOptions(CandyOptions candyOptions, Options cmdOptions, String[] args) {
		CommandLine cmd = new CommandLine(cmdOptions, args);
		if (cmd.hasOption("-h")) {
			prepareHelper(candyOptions, cmdOptions);
			return cmd;
		}
		buildCandyOptions(candyOptions, args);
		return cmd;
	}
	
	private static void buildCandyOptions(CandyOptions candyOptions, String[] args) {
		String toolName = "";
		if (args.length > 0) {
			toolName = args[0];
			if (CandyToolFactory.isTool(toolName)) {
				args = Arrays.copyOfRange(args, 1, args.length);
			}
		}
		candyOptions.tool = CandyToolFactory.getCandyTool(toolName);
		Options toolOps = new Options();
		defineToolOptions(toolOps, candyOptions.tool);
		candyOptions.cmdLine = new CommandLine(toolOps, args);
		candyOptions.options = toolOps;
		candyOptions.srcFiles = getInputFiles(
			candyOptions.cmdLine.getArgs()
		);
		String[] defaultArgs = candyOptions.cmdLine.getArgs();
		candyOptions.interpreterOptions = new InterpreterOptions(
			defaultArgs.length == 0 ? defaultArgs :
				Arrays.copyOfRange(defaultArgs, 1, defaultArgs.length)
		);
	}
	
	private static File[] getInputFiles(String... args) {
		if (args.length == 0) {
			return new File[0];
		}
		File f = new File(args[0]);
		if (!f.exists()) {
			throw new Options.ParseException(
				"Can't open file: " + f.getAbsolutePath());
		}
		ArrayList<File> inputFiles = new ArrayList<>();
		inputFiles.addAll(FilePathUtils.getFilesByBfsOrder(
			new File(args[0]), 
			CandyFileFilter.CANDY_FILE_FILTER
		));
		if (inputFiles.size() == 0) {
			throw new Options.ParseException(
				"Missing source files: " + f.getAbsolutePath());
		}
		return inputFiles.toArray(new File[0]);
	}

	private static void prepareHelper(CandyOptions candyOptions, Options options) {
		candyOptions.printHelper = true;
		candyOptions.options = options;
		for (CandyTool tool : CandyToolFactory.tools()) {
			defineToolOptions(candyOptions.options, tool);
		}
		candyOptions.options.build();
	}
	
	private static Options defineToolOptions(Options options, CandyTool tool) {
		String groupHelper = tool.groupHelper();
		String groupName = tool.groupName();
		String[] aliases = tool.aliases();
		if (aliases != null) {	
			groupName += String.format("(%s)", String.join(", ", aliases));
		}
		options.newGroup(groupName, groupHelper);
		tool.defineOptions(options);
		options.build();
		return options;
	}
}
