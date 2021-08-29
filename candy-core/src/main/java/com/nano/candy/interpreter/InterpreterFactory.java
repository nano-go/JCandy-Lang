package com.nano.candy.interpreter;

public class InterpreterFactory {
	
	/**
	 * Creates a new interpreter.
	 *
	 * @param opt_InterpreterOptions The specified options of the interpreter.
	 *                               This param is nullable.
	 *
	 * @return Returns the interpreter.
	 */
	public static Interpreter newInterpreter(InterpreterOptions opt_InterpreterOptions) {
		if (opt_InterpreterOptions == null) {
			opt_InterpreterOptions = new InterpreterOptions(new String[0]);
		}
		return new InterpreterImpl(opt_InterpreterOptions);
	}    
}
