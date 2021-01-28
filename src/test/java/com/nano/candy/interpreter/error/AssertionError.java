package com.nano.candy.interpreter.error;

public class AssertionError extends CandyRuntimeError{
	
	public AssertionError(){
		this("") ;
	}
	
	public AssertionError(String msg){
		super(msg) ;
	}
}
