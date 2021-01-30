package com.nano.candy.parser;

import com.nano.candy.utils.Characters;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Position;
import java.util.Arrays;

public class SourceCodeReader {

	protected final Logger logger = Logger.getLogger();
	
	private String curLineStr;

	private char[] buf;

	// The current character.
	private char ch;

	// The position of current character in the buf.
	private int bp;
	
	// The index of the first character of a line in buf.
	private int lineStart;
	private int lineLength;
	 
	private int line;
	private int col;
	
	private String fileName;

	private char[] savedBuf;
	private int sp;

	public SourceCodeReader(String filename, char[] input) {
		this.fileName = filename;
		this.savedBuf = new char[128];
		int len = input.length;
		input = Arrays.copyOf(input, len + 1);
		input[len] = Characters.EOF;
		this.ch = 1;
		this.buf = input;
		readNextChar();
	}
	
	private void nextLine() {
		if (col < lineLength) {
			return;
		}
		lineStart = bp;
		while (true) {
			char ch = buf[bp ++];
			if (ch == '\n' || ch == Characters.EOF) {
				lineLength = bp - lineStart;
				break;
			}
		}
		if (lineLength - 1 == 0) {
			curLineStr = "";
		} else {
			curLineStr = String.valueOf(buf, lineStart, lineLength - 1);
		}
		line ++ ;
		col = 0 ;
	}
	
	public void error(String message, Object... args) {
		error(pos(), message, args);
	}

	public void error(Position pos, String message, Object... args) {
		logger.error(pos, message, args);
	}

	public void warn(String message, Object... args) {
		warn(pos(), message, args);
	}

	public void warn(Position pos, String message, Object... args) {
		logger.warn(pos, message, args);
	}
	
	public Position pos() {
		return new Position(fileName, curLineStr(), curLine(), curCol());
	}
	
	public void consume() {
		readNextChar();
	}

	public void putChar(boolean consume) {
		putChar(peek());
		if (consume) {
			consume();
		}
	}

	public void putChar(char ch) {
		ensureCapacity();
		savedBuf[sp ++] = ch;
	}

	private void ensureCapacity() {
		if (sp >= savedBuf.length) {
			savedBuf = Arrays.copyOf(savedBuf, savedBuf.length * 2);
		}
	}
	
	public String savedString() {
		String savedString = String.valueOf(savedBuf, 0, sp);
		sp = 0;
		return savedString;
	}

	public char tryConvertToEscapeChar() {
		if (ch != '\\') {
			return ch;
		}
		this.ch = convertToEscapeChar();
		return ch;
	}
	
	private char convertToEscapeChar() {
		char ch = readNextChar();
		switch (ch) {
			case 't' : return '\t';
			case 'r' : return '\r';
			case 'n' : return '\n';
			case 'f' : return '\f';
			case 'b' : return '\b';
			case '\\' : return '\\';
			case '\'' : return '\'';
			case '\"' : return '\"';
		}
		error("Unexpected escape char: '\\%c'", ch);
		return ch;
	}
	
	public char peek() {
		return ch;
	}

	public char readNextChar() {
		if (!isAtEnd()) {
			nextLine();
			ch = buf[lineStart + col];
			col ++;
		}
		return ch;
	}

	public boolean isAtEnd() {
		return ch == Characters.EOF;
	}

	public int curLine() {
		return line;
	}

	public int curCol() {
		return col;
	}

	public String curLineStr() {
		return curLineStr;
	}
    
}
