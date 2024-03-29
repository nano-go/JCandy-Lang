package com.nano.candy.utils;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

public abstract class Logger {
	
	public static CandyLogger newLogger() {
		return new CandyLogger();
	}
	
	public boolean printAllMessage(boolean exitIfError) throws IOException {
		if (hadWarns()) {
			printWarns(System.out);
		}
		if (hadErrors()) {
			printErrors(System.err);
			clearAllMessages();
			if(exitIfError) {
				System.exit(1);
			}
			return false;
		}
		clearAllMessages();
		return true;
	}
	
	public void printErrors(OutputStream out) throws IOException {
		printErrors(new OutputStreamWriter(out));
	}
	public void printWarns(OutputStream out) throws IOException {
		printWarns(new OutputStreamWriter(out));
	}

	public void clearAllMessages() {
		clearErrors();
		clearWarns();
	}
	
	public abstract void error(Position pos, String message, Object... args);
	public abstract void warn(Position pos, String message, Object... args);
	public abstract boolean hadErrors();
	public abstract boolean hadWarns();
	public abstract void clearErrors();
	public abstract void clearWarns();
	public abstract List<LogMessage> getErrorMessages();
	public abstract List<LogMessage> getWarnMessages();
	
	
	public abstract void printErrors(Writer out) throws IOException;
	public abstract void printWarns(Writer out) throws IOException;
	
	public static class LogMessage {
		protected Position pos;
		protected String message;

		public LogMessage(Position pos, String message) {
			this.pos = pos;
			this.message = message;
		}

		public Position getPos() {
			return pos;
		}

		public String getMessage() {
			return message;
		}

		@Override
		public String toString() {
			// For Example:
			// Error: Invalid syntax!
			//     in source: ./src/test_script.cd
			//     15 | getTestCases(
			StringBuilder builder = new StringBuilder().append(message)
				.append("\n    in source: ")
				.append(pos.getFileName());
			if (pos.getLineFromSource().isPresent()) {
				String linePrefix = String.format("    %d | ", pos.getLine());
				String line = pos.getLineFromSource().get();
				String lineTrimmedLeft = line.replaceAll("^\\s+", "");
				builder.append("\n").append(linePrefix).append(lineTrimmedLeft);
				builder.append("\n")	
					.append(" ".repeat(linePrefix.length() + (pos.getCol() - 1) - 
						(line.length() - lineTrimmedLeft.length())))
					.append("^");
			}
			return builder.toString();
		}
	}
}
