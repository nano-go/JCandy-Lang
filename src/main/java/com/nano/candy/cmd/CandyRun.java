package com.nano.candy.cmd;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.InterpreterFactory;
import com.nano.candy.sys.CandySystem;

public class CandyRun {
	
	private static Interpreter interpreter = InterpreterFactory.newInterpreter("i2");
	
	private CandyOptions options; 
	
	public CandyRun(String... args) {
		this.options = CandyOptionsParser.parse(args);
	}
	
	public void main() throws Exception {
		CandySystem.init();
		if (options.isPrintHelper()) {
			options.printHelper();
			return;
		}
		interpreter.enter(options.interpreterOptions);
		options.tool.run(interpreter, options);
	}
}
