package com.nano.candy.parser;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Position;
import java.util.Arrays;

public abstract class SourceCodeReader {
	
	protected final Logger logger = Logger.getLogger() ;
	
	private String fileName ;
	
	private char[] savedChars ;
	private int savedCharsPosition ;
	
	public SourceCodeReader(String fileName) {
		this.fileName = fileName ;
		this.savedChars = new char[128] ;
	}
	
	public Position pos() {
		return new Position(fileName, curLineStr(), curLine(), curCol()) ;
	}
	
	public Position posWithoutLineStr() {
		return new Position(fileName, null, curLine(), curCol()) ;
	}
	
	public void consume() {
		readNextChar() ;
	}
	
	public void error(String message, Object... args) {
		error(pos(), message, args) ;
	}
	
	public void error(Position pos, String message, Object... args) {
		logger.error(pos, message, args) ;
	}
	
	public void warn(String message, Object... args) {
		warn(pos(), message, args) ;
	}
	
	public void warn(Position pos, String message, Object... args) {
		logger.warn(pos, message, args) ;
	}
	
	public void putChar(boolean consume) {
		putChar(peek()) ;
		if (consume) {
			consume() ;
		}
	}
	
	public void putChar(char ch) {
		ensureCapacity() ;
		savedChars[savedCharsPosition ++] = ch ;
	}
	
	public String savedString() {
		String savedString = String.valueOf(savedChars, 0, savedCharsPosition) ;
		savedCharsPosition = 0 ;
		return savedString ;
	}

	private void ensureCapacity() {
		if (savedCharsPosition >= savedChars.length) {
			savedChars = Arrays.copyOf(savedChars, savedChars.length * 2) ;
		}
	}
	
	public char tryConvertToEscapeChar() {
		char c = peek() ;
		if (c != '\\') {
			return c ;
		}
		c = readNextChar() ;
		switch (c) {
			case 't' : return '\t' ;
			case 'r' : return '\r' ;
			case 'n' : return '\n' ;
			case 'f' : return '\f' ;
			case 'b' : return '\b' ;
			case '\\' : return '\\' ;
			case '\'' : return '\'' ;
			case '\"' : return '\"' ;
		}
		error("Unexpected escape char: '\\%c'", c) ;
		return c ;
	}
	
	public abstract char peek() ;
	public abstract char readNextChar() ;
	public abstract boolean isAtEnd() ;

	public abstract int curLine() ;
	public abstract int curCol() ;
	public abstract String curLineStr() ;
	
}
