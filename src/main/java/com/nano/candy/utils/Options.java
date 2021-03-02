package com.nano.candy.utils;
import java.util.HashMap;
import java.util.LinkedList;

public class Options {
	
	public static class ParseException extends RuntimeException {
		
		public ParseException(String msg) {
			super(msg);
		}
	}
	
	protected static class Option {
		protected String name;
		protected boolean hasArg;
		protected String description;
		public Option(String name, boolean hasArg, String description) {
			this.name = name;
			this.hasArg = hasArg;
			this.description = description;
		}
	}
	
	public static class Group {
		protected HashMap<String, Option> options;
		protected String name;
		
		public Group(String name) {
			this.name = name;
			this.options = new HashMap<>();
		}
	}
	
	private LinkedList<Group> groups;
	protected HashMap<String, Option> options;
	
	public Options() {
		this.groups = new LinkedList<>();
		this.options = new HashMap<>();
		this.groups.add(new Group("Common"));
	}
	
	public Options newGroup(String name) {
		this.groups.add(new Group(name));
		return this;
	}
	
	public Options addOption(String name, boolean hasArg, String description) {
		if (this.options.containsKey(name)) {
			throw new ParseException("The same name: " + name);
		}
		Group group = this.groups.getLast();
		Option option = new Option(name, hasArg, description);
		group.options.put(name, option);
		options.put(name, option);
		return this;
	}
	
	protected Option getOption(String name) {
		return options.get(name);
	}
	
	public String helper() {
		StringBuilder builder = new StringBuilder();
		for (Group group : groups) {
			if (group.options.size() == 0) {
				continue;
			}
			builder.append(group.name).append(":\n");
			optionHelper(builder, group.options);
			builder.append("\n");
		}
		return builder.toString();
	}

	private void optionHelper(StringBuilder builder, HashMap<String, Option> options) {
		int space = 10;
		String tab = "    ";
		for (Option op : options.values()) {
			builder.append(tab).append(op.name).append(
				" ".repeat(space - op.name.length()));
			boolean isFirst = true;
			for (String line : op.description.split(System.lineSeparator())) {
				if (isFirst) {
					isFirst = false;
					builder.append(line);
					continue;
				}
				builder.append("\n")
					.append(tab)
					.append(" ".repeat(space));
				builder.append(line);
			}
		}
	}
	
}
