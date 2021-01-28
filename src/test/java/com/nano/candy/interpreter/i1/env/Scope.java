package com.nano.candy.interpreter.i1.env;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import java.util.Optional;

public interface Scope {
	
	/**
	 * Returns enclosing scope.
	 * 
	 * @return Nullable
	 */
	public Scope getOutterScope() ;
	public Optional<Variable> lookupVariable(String name) ;
	public Optional<Variable> lookupVariableInCurrentScope(String name) ;
	public Variable defineVariable(String name, CandyObject reference) ;
}
