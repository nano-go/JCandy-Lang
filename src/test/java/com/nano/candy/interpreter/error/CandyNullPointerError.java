package com.nano.candy.interpreter.error;
import com.nano.candy.interpreter.i1.env.Variable;

public class CandyNullPointerError extends CandyRuntimeError {
	
	public CandyNullPointerError(Variable variable) {
		super(String.format("The variable '%s' is a null pointer.", variable.getVariableName())) ;
	}
	
	public CandyNullPointerError(String messageFormat, Object... args) {
		super(String.format(messageFormat, args)) ;	
	}
}
