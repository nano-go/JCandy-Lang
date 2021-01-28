package com.nano.candy.parser;
import com.nano.candy.utils.Position;

public interface Scanner {
	public Token nextToken();
	public Token peek();
	public boolean hasNextToken();
	public Position basePos();
}
