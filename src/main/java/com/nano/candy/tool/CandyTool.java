package com.nano.candy.tool;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.main.CandyOptions;
import com.nano.candy.utils.Options;

public interface CandyTool {
	public void defineOptions(Options options);
	public void run(Interpreter interpreter, CandyOptions options) throws Exception;
}
