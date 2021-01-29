package com.nano.candy.parser;

import com.nano.candy.utils.Characters;
import java.util.Arrays;
import java.util.Objects;

public class CandySourceCodeReader extends SourceCodeReader {

	private String currentLineStr;

	private int lineStart;
	private int lineLength;
	
	private int currentLineNumber;
	private int currentColumnNumber;

	private char[] in;
	private char   currentChar;
	private int    currentCharPosition;

	public CandySourceCodeReader(String filename, char[] in) {
		super(filename);
		this.in = Objects.requireNonNull(in);
		int len = in.length;
		this.in = Arrays.copyOf(in, len + 1);
		this.in[len] = Characters.EOF;
		this.currentChar = 1;
		readNextChar();
	}
	
	private void fullLineBuffer() {	
		if(currentColumnNumber < lineLength) {
			return;
		}
		lineStart = currentCharPosition;
		while(true) {
			char ch = in[currentCharPosition ++] ;
			if(ch == '\n' || ch == Characters.EOF) {
				lineLength = currentCharPosition - lineStart;
				break ;
			}
		}
		if (lineLength - 1 == 0) {
			currentLineStr = "";
		} else {
			currentLineStr = String.valueOf(in, lineStart, lineLength - 1);
		}
		currentLineNumber ++ ;
		currentColumnNumber = 0 ;
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
		if (currentChar != Characters.EOF) {
			fullLineBuffer();
			currentChar = in[lineStart + currentColumnNumber];
			currentColumnNumber ++;
		}
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
