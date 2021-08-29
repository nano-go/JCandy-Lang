package com.nano.candy.cmd;

import com.nano.candy.interpreter.Interpreter;

public interface CandyTool {

	public String groupName();
	public String groupHelper();
	public String[] aliases();
	public void defineOptions(Options options);
	public void run(Interpreter interpreter, CandyOptions options) throws Exception;
}

