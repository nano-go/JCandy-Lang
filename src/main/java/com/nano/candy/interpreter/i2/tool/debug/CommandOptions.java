package com.nano.candy.interpreter.i2.tool.debug;
import java.util.ArrayList;
import java.util.List;

public class CommandOptions {
	
	public enum OptionType {
		FLAG,
		WITH_ARGUMENT,
		STRING,
		INTEGER,
		NUMBER,
	}
	
	public static class Option {
		protected final OptionType type;
		protected final String name;
		protected final boolean optional;
		protected final String description;

		public Option(OptionType type, String name, boolean optional, String description) {
			this.type = type;
			this.name = name;
			this.optional = optional;
			this.description = description;
		}

		public OptionType getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		public boolean isOptional() {
			return optional;
		}

		public String getDescription() {
			return description;
		}
	}
	
	private List<Option> options;
	
	public CommandOptions() {
		options = new ArrayList<>();
	}
	
	public CommandOptions addOption(OptionType type, String name, boolean optional, String description) {
		options.add(new Option(type, name, optional, description));
		return this;
	}
	
	public List<Option> getOptions() {
		return options;
	}
}
