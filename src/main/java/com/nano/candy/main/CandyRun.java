package com.nano.candy.main;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.InterpreterFactory;

public class CandyRun {
	
	private static Interpreter interpreter = InterpreterFactory.newInterpreter("i2");
	
	private CandyOptions options; 
	
	public CandyRun(String... args) {
		this.options = CandyOptionsParser.parse(args);
	}
	
	public void main() throws Exception {
		if (options.isPrintHelper()) {
			options.printHelper();
			return;
		}
		interpreter.enter(options.interpreterOptions);
		options.tool.run(interpreter, options);
	}


}
