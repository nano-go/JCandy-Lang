package com.nano.candy.parser;

import com.nano.candy.utils.Characters;
import java.util.Arrays;
import java.util.Objects;

public class CandySourceCodeReader extends SourceCodeReader {

	private StringBuffer currentLineBuffer;
	private String       currentLineStr;
	private int          currentLineNumber;
	private int          currentColumnNumber;

	private char[] in;
	private char   currentChar;
	private int    currentCharPosition;

	public CandySourceCodeReader(String filename, char[] in) {
		super(filename);
		this.in = Objects.requireNonNull(in);
		this.in = Arrays.copyOf(in, in.length + 1);
		this.in[this.in.length - 1] = Characters.EOF;
		this.currentLineBuffer = new StringBuffer();
		this.currentChar = '\1';
		readNextChar();
	}
	
	private void fullLineBuffer() {	
		if(currentColumnNumber < currentLineBuffer.length()) {
			return;
		}
		
		clearLineBuffer();
		while(true) {
			char ch = in[currentCharPosition ++];

			if(ch == Characters.EOF || ch == '\n') {
				currentLineStr = currentLineBuffer.toString();
				currentLineBuffer.append(ch);
				break;
			}
			currentLineBuffer.append(ch);
		}

		currentLineNumber ++;
		currentColumnNumber = 0;
	}

	private void clearLineBuffer() {
		this.currentLineBuffer.delete(0, this.currentLineBuffer.length());
	}

	@Override
	public char tryConvertToEscapeChar() {
		currentChar = super.tryConvertToEscapeChar();
		return currentChar;
	} 
	
	@Override
	public char peek() {
		return currentChar;
	}

	@Override
	public char readNextChar() {
		if (isAtEnd()) {
			return currentChar;
		}
		fullLineBuffer();
		currentChar = currentLineBuffer.charAt(currentColumnNumber ++);
		return currentChar;
	}

	@Override
	public boolean isAtEnd() {
		return currentChar == Characters.EOF;
	}

	@Override
	public int curLine() {
		return currentLineNumber;
	}

	@Override
	public int curCol() {
		return currentColumnNumber;
	}

	@Override
	public String curLineStr() {
		return currentLineStr;
	}
    
}
