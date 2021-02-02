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
		return new Position(fileName, currentLine(), line(), col());
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
	
	public char escapeChar() {
		if (ch != '\\') {
			return ch;
		}
		return convertToEscapeChar();
	}
	
	private char convertToEscapeChar() {
		char ch = readNextChar();
		switch (ch) {	
			case '\\' :
			case '\'' : 
			case '\"' :
				return ch;
				
			case 't' : return '\t';
			case 'r' : return '\r';
			case 'n' : return '\n';
			case 'f' : return '\f';
			case 'b' : return '\b';
			
			case '0': case '1': case '2': case '3': 
			case '4': case '5': case '6': case '7':
				return convertToUnicodeChar(3, 8, 255);
	
			case 'u':
				// consume 'u'
				consume();
				return convertToUnicodeChar(4, 16, Character.MAX_CODE_POINT);
		}
		error("Unexpected escape char: '\\%c'", ch);
		return ch;
	}
	
	private char convertToUnicodeChar(int n, int base, int max) {
		int unicodeChar = 0;
		int i = 0;
		n --;
		while (true) {
			char ch = peek();
			int d = base;
			if (ch >= '0' && ch <= '9') {	
				d = ch - '0';
			} else {
				char hex = Characters.lower(ch);
				if (hex >= 'a' && hex <= 'f') {
					d = hex - 'a' + 10;
				}
			}
			if (d >= base) {
				error("illegal unicode escape: '%c'", ch);
				return ch;
			}
			unicodeChar = unicodeChar * base + d;
			if (i == n) {
				break;
			}
			i ++;
			consume();
		}
		if (unicodeChar > max) {
			error("escape is invalid Unicode code point 0x%X", unicodeChar);
			return ch;
		}
		return (char)unicodeChar;
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

	public int line() {
		return line;
	}

	public int col() {
		return col;
	}

	public String currentLine() {
		return curLineStr;
	}
    
}
