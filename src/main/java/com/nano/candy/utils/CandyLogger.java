package com.nano.candy.utils;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class CandyLogger extends Logger {
	
	private List<LogMessage> errors;
	private List<LogMessage> warns;
	
	protected CandyLogger() {
		errors = new ArrayList<>();
		warns = new ArrayList<>();
	}
	
	@Override
	public void error(Position pos, String message, Object... args) {
		errors.add(new LogMessage(pos, String.format(message, args)));
	}

	@Override
	public void warn(Position pos, String message, Object... args) {
		warns.add(new LogMessage(pos, String.format(message, args)));
	}

	@Override
	public boolean hadErrors() {
		return !errors.isEmpty();
	}

	@Override
	public boolean hadWarns() {
		return !warns.isEmpty();
	}

	@Override
	public List<Logger.LogMessage> getErrorMessages() {
		return errors;
	}

	@Override
	public List<Logger.LogMessage> getWarnMessages() {
		return warns;
	}
	
	@Override
	public void clearErrors() {
		errors.clear();
	}

	@Override
	public void clearWarns() {
		warns.clear();
	}

	@Override
	public void printErrors(Writer out) throws IOException {
		printMessages(out, "Error: ", errors);
	}

	@Override
	public void printWarns(Writer out) throws IOException {
		printMessages(out, "Warn: ", warns);
	}

	private void printMessages(Writer out, String prefix, List<LogMessage> messages) throws IOException {
		StringBuilder msgStr = new StringBuilder();
		for (LogMessage msg : messages) {
			msgStr.append(prefix)
				.append(msg.toString())
				.append("\n\n");
		}
		out.write(msgStr.toString());
		out.flush();
	}

}
