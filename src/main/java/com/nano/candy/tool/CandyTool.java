package com.nano.candy.tool;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.main.CandyOptions;

public interface CandyTool {
	
	public void run(Interpreter interpreter, CandyOptions options) throws Exception;
}
