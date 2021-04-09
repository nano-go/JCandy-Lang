package com.nano.candy.interpreter.i2.tool.debug;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandLine {
	
	public static class ParserException extends Exception {
		public ParserException(String msg) {
			super(msg);
		}
	}

	private String commandName;
	private CommandOptions options;
	private HashMap<String, Object> parsedOptions;
	private String[] otherArguments;
	
	public CommandLine(Command cmd, CommandOptions options) {
		this(cmd.name(), options);
	}
	
	public CommandLine(String commandName, CommandOptions options) {
		this.commandName = commandName;
		this.options = options;
		this.parsedOptions = new HashMap<>();
	}
	
	public String[] getArgs() { return otherArguments; }
	
	public int getInteger(String name) {
		return (Integer) parsedOptions.get(name);
	}
	
	public int getInteger(String name, int def) {
		if (hasOption(name)) {
			return (Integer) parsedOptions.get(name);
		}
		return def;
	}
	
	public String getString(String name) {
		return (String) parsedOptions.get(name);
	}
	
	public String getString(String name, String def) {
		if (hasOption(name)) {
			return (String) parsedOptions.get(name);
		}
		return def;
	}
	
	public double getNumber(String name) {
		return (Double) parsedOptions.get(name);
	}
	
	public double getNumber(String name, double def) {
		if (hasOption(name)) {
			return (Double) parsedOptions.get(name);
		}
		return def;
	}
	
	public String getOptionArg(String name) {
		return getString(name);
	}

	public String getOptionArg(String name, String def) {
		return getString(name, def);
	}
	
	public boolean hasOption(String name) {
		return parsedOptions.containsKey(name);
	}
	
	public CommandLine parse(String[] args) throws ParserException {
		int argsI = 0, opsI = 0;
		List<CommandOptions.Option> options = this.options.getOptions();
		
		while (opsI < options.size()) {
			if (argsI >= args.length) {
				checkUnparsedOptions(opsI, options);
				break;
			}
			argsI = parseOption(options.get(opsI), args, argsI);
			opsI ++;
		}
		
		this.otherArguments = Arrays.copyOfRange(args, argsI, args.length);
		return this;
	}

	private void checkUnparsedOptions(int opsI, List<CommandOptions.Option> options) throws ParserException {
		while (opsI < options.size()) {
			if (!options.get(opsI).optional) {
				throw new ParserException(String.format(
					"'%s' must be followed by the option '%s'.",
					commandName, options.get(opsI).name
				));
			}
			opsI ++;
		}
	}

	private int parseOption(CommandOptions.Option option, String[] args, int i) throws ParserException {
		switch (option.type) {
			case WITH_ARGUMENT:
				return parseOptionWithArg(args, i, option);
			case FLAG:
				return parseFlag(args, i, option);
			case INTEGER:
				return parseInteger(args, i, option);
			case NUMBER:
				return parseNumber(args, i, option);
			case STRING:
				return parseString(args, i, option);
		}
		throw new Error("Unreachable");
	}
	
	private int parseOptionWithArg(String[] args, int i, CommandOptions.Option option) throws ParserException {
		if (!args[i].equals(option.name)) {
			checkOptional(option);
			return i;
		}
		if (i >= args.length - 1) {
			throw new ParserException(String.format(
				"'%s' must be followed by a argument.",
				option.name
			));
		}
		parsedOptions.put(option.name, args[i + 1]);
		return i + 2;
	}
	
	private int parseFlag(String[] args, int i, CommandOptions.Option option) throws ParserException {
		if (!option.name.equals(args[i])) {
			checkOptional(option);
			parsedOptions.put(option.name, true);
			return i;
		}
		return i + 1;
	}

	private int parseInteger(String[] args, int i, CommandOptions.Option option) throws ParserException {
		try {
			int num = Integer.parseInt(args[i]);
			parsedOptions.put(option.name, num);
			return i + 1;
		} catch (NumberFormatException e) {
			checkOptional(option);
			return i;
		}
	}
	
	private int parseNumber(String[] args, int i, CommandOptions.Option option) throws ParserException {
		try {
			double num = Double.parseDouble(args[i]);
			parsedOptions.put(option.name, num);
			return i + 1;
		} catch (NumberFormatException e) {
			checkOptional(option);
			return i;
		}
	}
	
	private int parseString(String[] args, int i, CommandOptions.Option option) {
		parsedOptions.put(option.name, args[i]);
		return i + 1;
	}

	private void checkOptional(CommandOptions.Option option) throws ParserException {
		if (!option.optional) {
			throw new ParserException(String.format(
				"'%s' must be followed by the option '%s'.\n",
				commandName, option.name
			));
		}
	}
}
