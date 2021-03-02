package com.nano.candy.main;

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
		Options options = defineOptions();
		parseOptions(candyOptions, options, args);
		return candyOptions;
	}

	private static Options defineOptions() {
		return new Options()
			.addOption("-h", false, "Print command line helper.");
	}
	
	private static CommandLine parseOptions(CandyOptions candyOptions, Options options, String[] args) {
		CommandLine cmd = new CommandLine(options, args);
		if (cmd.hasOption("-h")) {
			candyOptions.printHelper = true;
			Options toolOps = new Options();
			for (CandyTool tool : CandyToolFactory.tools()) {
				tool.defineOptions(toolOps);
			}
			candyOptions.options = toolOps;
			return cmd;
		}
		
		String toolName = "";
		if (args.length > 0) {
			toolName = args[0];
			if (CandyToolFactory.isTool(toolName)) {
				args = Arrays.copyOfRange(args, 1, args.length);
			}
		}
		candyOptions.tool = CandyToolFactory.createCandyTool(toolName);
		Options toolOps = new Options();
		candyOptions.tool.defineOptions(toolOps);
		candyOptions.cmdLine = new CommandLine(toolOps, args);
		candyOptions.options = toolOps;
		candyOptions.srcFiles = getInputFiles(
			candyOptions.cmdLine.getArgs());
		return cmd;
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
}
