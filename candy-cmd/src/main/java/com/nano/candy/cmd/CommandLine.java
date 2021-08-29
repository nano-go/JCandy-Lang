package com.nano.candy.cmd;
import java.util.ArrayList;
import java.util.HashMap;

public class CommandLine {
	
	private Options options;
	private String[] args;
	
	private HashMap<String, String> reconginizedOptions;

	public CommandLine(Options options, String[] args) {
		this.options = options;
		this.reconginizedOptions = new HashMap<>();
		parse(args);
	}

	private void parse(String[] args) {
		ArrayList<String> defArgs = new ArrayList<>(args.length);
		for (int i = 0; i < args.length; i ++) {
			String flag = args[i];
			Options.Option option = options.getOption(flag);
			if (option == null) {
				defArgs.add(flag);
				continue;
			}
			String argument = null;
			if (option.hasArg) {
				if (i >= args.length-1 || 
				    !checkValidCmdArgName(args[i + 1])) {
					throw new Options.ParseException(
						"Missing argument for option: " + flag);
				}
				argument = args[i + 1];
				i ++;
			}
			reconginizedOptions.put(option.name, argument);
		}
		this.args = defArgs.toArray(new String[0]);
	}
	
	private static boolean checkValidCmdArgName(String name) {
		return !name.startsWith("-");
	}
	
	public Options getOptions() {
		return options;
	}
	
	public boolean hasOption(String name) {
		return reconginizedOptions.containsKey(name);
	}
	
	public String getOptionArg(String name) {
		return reconginizedOptions.get(name);
	}
	
	public String[] getArgs() {
		return args;
	}
}
