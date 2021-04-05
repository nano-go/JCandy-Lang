package com.nano.candy.utils;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

public class Options {
	
	public static class ParseException extends RuntimeException {
		public ParseException(String msg) {
			super(msg);
		}
	}
	
	protected static class Option {
		protected final String name;
		protected final boolean hasArg;
		protected final String description;
		public Option(String name, boolean hasArg, String description) {
			this.name = name;
			this.hasArg = hasArg;
			this.description = description == null ? "" : description;
		}

		public String getName() {
			return name;
		}

		public boolean hasArg() {
			return hasArg;
		}

		public String getDescription() {
			return description;
		}
	}
	
	public static class Group {
		protected HashMap<String, Option> options;
		protected String name;
		protected Optional<String> description;
		
		public Group(String name, String description) {
			this.name = name;
			this.description = Optional.ofNullable(description);
			this.options = new HashMap<>();
		}
		
		public Group addOption(String name, boolean hasArg, String description) {
			if (this.options.containsKey(name)) {
				throw new ParseException("The same option name: " + name);
			}
			Option option = new Option(name, hasArg, description);
			options.put(name, option);
			return this;
		}
		
		public String getDescription() {
			return description.orElse("");
		}
		
		public Collection<Option> getOptions() {
			return options.values();
		}
	}
	
	private LinkedList<Group> groups;
	protected HashMap<String, Option> options;
	
	public Options() {
		this.groups = new LinkedList<>();
		this.options = new HashMap<>();
		this.groups.add(new Group("Normal Options", "candy [-options...]"));
	}
	
	public Options newGroup(String name) {
		return newGroup(name, null);
	}
	
	public Options newGroup(String name, String description) {
		this.groups.add(new Group(name, description));
		return this;
	}
	
	/**
	 * Creates a new option to the last group.
	 */
	public Options addOption(String name, boolean hasArg, String description) {
		groups.getLast().addOption(name, hasArg, description);
		return this;
	}
	
	public Group getGroup(int index) {
		return groups.get(index);
	}
	
	/**
	 * Returns the option to which the specified name is mapped or null
	 * if this options has not been built.
	 */
	public Option getOption(String name) {
		return options.get(name);
	}
	
	public Options build() {
		for (Group group : groups) {
			options.putAll(group.options);
		}
		return this;
	}
	
	private static final String TAB = "    ";
	public String helper() {
		StringBuilder builder = new StringBuilder();
		for (Group group : groups) {
			groupHelper(builder, group);
			builder.append("\n");
		}
		return builder.toString();
	}

	private void groupHelper(StringBuilder builder, Group group) {
		builder.append(group.name).append("\n");
		if (group.description.isPresent()) {
			printByMultiLine(TAB, builder, group.description.get());
		}
		printOptions(builder, group);
	}

	private void printOptions(StringBuilder builder, Group group) {
		if (group.options.isEmpty()) {
			return;
		}
		builder.append("\n");
		for (Option option : group.options.values()) {
			printOption(builder, option);
		}
	}

	private void printOption(StringBuilder builder, Option option) {
		final int MAX_NAME_LEN = 15;
		final String SPACE = " ".repeat(MAX_NAME_LEN);
		final String OPTION_NAME_FORMAT = "%-" + MAX_NAME_LEN + "s";
		
		builder.append(TAB)
			.append(String.format(OPTION_NAME_FORMAT, option.name));
		printDescription(builder, option, SPACE);
	}

	private void printDescription(StringBuilder builder, Option option, final String SPACE) {
		String[] description = option.description.split(System.lineSeparator());
		builder.append(description[0]).append('\n');
		for (int i = 1; i < description.length; i ++) {
			builder.append(TAB)
				.append(SPACE)
				.append(description[i])
				.append('\n');
		}
	}
	
	private void printByMultiLine(final String TAB, StringBuilder builder, String text) {
		for (String line : text.split(System.lineSeparator())) {
			builder.append(TAB).append(line).append("\n");
		}
	}
	
}
