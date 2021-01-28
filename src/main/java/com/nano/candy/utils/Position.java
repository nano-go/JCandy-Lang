package com.nano.candy.utils;
import java.util.Objects;
import java.util.Optional;

public class Position {
	
	private final String fileName;
	private final Optional<String> lineFromSource;
	private final int col;
	private final int line;

	public Position(String fileName, String lineFromSource, int line, int col) {
		this.fileName = fileName;
		this.lineFromSource = Optional.ofNullable(lineFromSource);
		this.col = col;
		this.line = line;
	}

	public String getFileName() {
		return fileName;
	}
	
	public Optional<String> getLineFromSource() {
		return lineFromSource;
	}

	public int getCol() {
		return col;
	}

	public int getLine() {
		return line;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Position) {
			Position pos = (Position) obj;
			return    col == pos.col 
			       && line == pos.line
				   && Objects.equals(getLineFromSource(), pos.getLineFromSource());
		}
		return false;
	}

	@Override
	public String toString() {
		return com.google.common.base.MoreObjects.toStringHelper(Position.class)
			.add("file name", fileName)
			.add("line text", getLineFromSource().get())
			.add("line", line)
			.add("col", col)
			.toString();
	}
	
}
