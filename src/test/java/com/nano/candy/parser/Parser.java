package com.nano.candy.parser;
import com.nano.candy.ast.Program;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Position;
import java.util.LinkedList;

public interface Parser {
    
	public Program parse() ;
}
